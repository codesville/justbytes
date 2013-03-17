'''
Created on Mar 16, 2013

@author: ubunty
'''
from google.appengine.ext import webapp
import models
import logging


class ImageHandler(webapp.RequestHandler):
    def get(self):
        businessId = self.request.get('businessName')
        logging.info(businessId)
        be = models.getBusinessImage(businessId)
        self.response.headers['Content-Type']='image/jpeg'
        self.response.out.write(be.picture)
        
if __name__ == '__main__':
    pass