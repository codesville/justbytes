import os
import models
import contenthandler
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.webapp import template


class MainPage(webapp.RequestHandler):
    
    
    def get(self):
        #self.response.headers['Content-Type'] = 'text/html'
        path=os.path.join(os.path.dirname(__file__),'view','index.html')
        #models.insertBe()
        rs = models.getAllBusinessEntities()
        
        self.response.out.write(template.render(path, {"title":"Just Bytes","beList":rs}))


application = webapp.WSGIApplication([('/', MainPage),
                                      ('/icons', contenthandler.ImageHandler)], debug=True)


def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
