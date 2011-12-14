#!/usr/bin/env python2.5
"""
The server places user reports into the database, allows
admins to process the database for statistics and displays
the results of previously processed statistics.

We don't need strong consistency guarantees, so we
don't do anything special with ancestor queries or parent keys.
"""

# magic incantiation to use up-to-date django
import os
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'
from google.appengine.dist import use_library
use_library('django', '1.2')

import cgi, hashlib, os

from google.appengine.ext import db
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

options = {
    'debug': True,
    'showForged': True,
    'testForm': True,
    'verify_inline': True,
    'secret': 'no cheating, please',
    'admins': ['tim.newsham@gmail.com',
                'alex@isecpartners.com',
                'alex@stamos.org',
                'andrew@becherer.org',
                'amandacrowell85@gmail.com',
                'peter.eckersley@gmail.com',
                'jesse.burns@gmail.com',
                ],
}

VERIFIED_NONE, VERIFIED_NO, VERIFIED_YES = -1,0,1

class Report(db.Model) :
    """A single report submitted from an app user."""
    version = db.IntegerProperty()
    when = db.DateTimeProperty(auto_now_add=True)
    src = db.IntegerProperty() # anonymized IP address
    os = db.StringProperty()
    phone = db.StringProperty()
    carrier = db.StringProperty()
    features = db.IntegerProperty() # bit vector
    log = db.TextProperty()
    auth = db.StringProperty() # sha hash
    verified = db.IntegerProperty() # VERIFIED_*

def verify(r) :
    def hash(s) :
        return hashlib.sha256(s).hexdigest()
    s = "secret=%s:version=%d:os=%s:phone=%s:carrier=%s:features=%d:log=%s" % (options['secret'], r.version, r.os, r.phone, r.carrier, r.features, r.log)
    auth = hash(s)
    if r.auth == auth :
        r.verified = VERIFIED_YES
    else :
        r.verified = VERIFIED_NO

def unIp(x) :
    try :
        a,b,c,d = map(int, x.split('.'))
    except ValueError :
        return 0

    for x in a,b,c,d :
        if not (0 <= x and x <= 255) :
            return 0
    return (a << 24) | (b << 16) | (c << 8) | d

def obfSrc(x) :
    """
    Discard every other bit, and the (likely) host field from the IP address.
    This will leave 4096 unique values (2^(4*3)).
    """
    return unIp(x) & 0x55555500

class ReportPage(webapp.RequestHandler) :
    """
    Posts from the android app are placed into the database.
    """
    def post(self) :
        try :
            version = int(self.request.get('version'))
            features = int(self.request.get('features'))
        except ValueError :
            errorPage(self, error='Invalid Parameter')
            return

        r = Report()
        r.src = obfSrc(self.request.remote_addr)
        r.version = version
        r.os = self.request.get('os')
        r.phone = self.request.get('phone')
        r.carrier = self.request.get('carrier')
        r.features = features
        r.log = self.request.get('log')
        r.auth = self.request.get('auth')
        r.verified = VERIFIED_NONE
        
        if options['verify_inline'] :
            verify(r)
        r.put()

        self.response.headers['Content-Type'] = 'text/xml'
        self.response.out.write('<response><status>0</status><message>Thank You.</message></response>')

def templ(self, path, **kw) :
    """Emit a template."""
    p = os.path.join(os.path.join(os.path.dirname(__file__), path))
    self.response.out.write(template.render(p, kw))

def errorPage(self, **kw) :
    return templ(self, 'error.html', **kw)

def allowAdmins(self, onAdmin) :
    user = users.get_current_user()
    if user :
        name = user.email()
        if name in options['admins'] :
            onAdmin(user)
        else :
            errorPage(self, error='Permission Denied for %s' % cgi.escape(name))
    else :
        self.redirect(users.create_login_url(self.request.uri))

class AdminPage(webapp.RequestHandler) :
    def get(self) :
        allowAdmins(self, self.onAdmin)
    def onAdmin(self, user) :
        templ(self, "admin.html")

def getWithAdd(d, k, f) :
    if k not in d :
        d[k] = f()
    return d[k]

class StatPage(webapp.RequestHandler) :
    """
    Generate stats.  XXX right now we generate and output
    stats.. later we should generate stats once, and cache and
    later just report the pregenerated stats.
    """
    def get(self) :
        allowAdmins(self, self.onAdmin)

    def stats(self) :
        cnt, verified, forged = 0, 0, 0
        phones, carriers, oses = set(), set(), set()
        dat = dict()
        for r in Report.all() :
            cnt += 1
            if r.verified == VERIFIED_NO :
                forged += 1
            if r.verified == VERIFIED_YES :
                verified += 1

            if r.verified == VERIFIED_NO :
                if not options['showForged'] :
                    continue
                r.phone = "FORGED:" + r.phone
                r.carrier = "FORGED:" + r.carrier
                r.os = "FORGED:" + r.os

            phones.add(r.phone)
            carriers.add(r.carrier)
            oses.add(r.os)

            k = r.phone, r.carrier, r.os
            d = getWithAdd(dat, k, dict)
            s = getWithAdd(d, r.features, lambda : [set(), 0])
            s[0].add(r.src)
            s[1] += 1

        def unvec(x) :
            l = [str(n) for n in xrange(64) if (x & (1L<<n))]
            if not l :
                return 'none'
            return ','.join(l)

        # post-process dat to get sorted popularity counts
        r = []
        for k in sorted(dat.keys()) :
            d = dat[k]
            pops = [(len(s[0]), s[1], unvec(k2)) for k2,s in d.items()]
            pops.sort(reverse=True)
            r.append((k, pops))

        return {
            'cnt': cnt,
            'verified': verified,
            'forged': forged,
            'phones': sorted(phones),
            'oses': sorted(oses),
            'carriers': sorted(carriers),
            'dat': r,
        }

    def onAdmin(self, user) :
        s = self.stats()
        templ(self, 'stats.html', **s)
        
class LogPage(webapp.RequestHandler) :
    def get(self):
        allowAdmins(self, self.onAdmin)

    def onAdmin(self, u) :
        try :
            s = self.request.get('n', None)
            n = 0
            if s is not None :
                n = int(s)
        except ValueError :
            errorPage(self, error='Invalid Parameter')
            return
        wr = self.response.out.write

        log = { 'found': False, 'id': n }
        # XXX is there a simpler query for id?
        if s is not None :
            for r in Report.gql("WHERE __key__ = :1", db.Key.from_path('Report', n)) :
                log['found'] = True
                log['id'] = r.key().id()
                log['data'] = cgi.escape(r.log)
                break
        templ(self, 'viewlog.html', **log)

class MainPage(webapp.RequestHandler) :
    def get(self):
        templ(self, 'main.html')

class TestForm(webapp.RequestHandler) :
    def get(self) :
        allowAdmins(self, self.onAdmin)
    def onAdmin(self, u) :
        if not options['testForm'] :
            errorPage(self, error='Disabled')
            return
        templ(self, 'test.html')

application = webapp.WSGIApplication([
    ('/', MainPage),
    ('/report', ReportPage),
    ('/admin', AdminPage),
    ('/admin/stats', StatPage),
    ('/admin/viewlog', LogPage),
    ('/admin/test', TestForm),
], debug=options['debug'])


def main():
  run_wsgi_app(application)

if __name__ == '__main__':
  main()
