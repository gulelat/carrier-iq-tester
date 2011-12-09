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
    'testForm': True,
    'verify_inline': True,
    'secret': 'no cheating, please',
    'admins': ['tim.newsham@gmail.com'],
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
    auth = db.StringProperty() # sha hash
    verified = db.IntegerProperty() # VERIFIED_*

def verify(r) :
    def hash(s) :
        return hashlib.sha256(s).hexdigest()
    # XXX find a good way to print out the when field for this...
    auth = hash("secret=%s:when=%s:os=%r:phone=%r:carrier=%r:features=%d" % (options['secret'], r.when, r.os, r.phone, r.carrier, r.features))
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
        # XXX
        wr = self.response.out.write
        wr('<h1>Admin Page</h1>')
        wr('<p>Welcome %s!' % (cgi.escape(user.email())))
        wr('<p>src is: %s' % (self.request.remote_addr))
        x = obfSrc(self.request.remote_addr)
        wr('<p>obfSrc is: %x' % (x))
        wr('<p>unIp is: %x' % (unIp(self.request.remote_addr)))

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
            if r.verified == VERIFIED_YES :
                verified += 1
            if r.verified == VERIFIED_NO :
                forged += 1
            phones.add(r.phone)
            carriers.add(r.carrier)
            oses.add(r.os)

            k = r.phone, r.carrier, r.os
            d = getWithAdd(dat, k, dict)
            s = getWithAdd(d, r.features, set)
            s.add(r.src)

        # post-process dat to get sorted popularity counts
        r = []
        for k in sorted(dat.keys()) :
            d = dat[k]
            pops = [(len(s), k2) for k2,s in d.items()]
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
        

class MainPage(webapp.RequestHandler) :
    def get(self):
        templ(self, 'main.html')

class TestForm(webapp.RequestHandler) :
    def get(self) :
        if not options['testForm'] :
            errorPage(self, error='Disabled')
            return
        self.response.out.write('''<form action="/report" method="post">
<br>Version:<input name="version" value="0">
<br>OS:<input name="os" value="">
<br>Phone:<input name="phone" value="">
<br>Carrier:<input name="carrier" value="">
<br>Features:<input name="features" value="">
<br>Auth:<input name="auth" value="">
<br><input type="submit" name="submit">''')

application = webapp.WSGIApplication([
    ('/report', ReportPage),
    ('/admin', AdminPage),
    ('/admin/stats', StatPage),
    ('/test', TestForm),
    ('/', MainPage),
], debug=options['debug'])


def main():
  run_wsgi_app(application)

if __name__ == '__main__':
  main()
