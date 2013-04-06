'''
Created on Mar 16, 2013

@author: ubunty
'''
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
import models
import logging
import os

# Renders the dynamic icons from the datastore
class ImageHandler(webapp.RequestHandler):
    def get(self):
        name = self.request.get('businessName')
        be = models.getBusinessEntity(name)
        self.response.headers['Content-Type']='image/jpeg'
        self.response.out.write(be.picture)

# Renders the Dining page        
class DiningHandler(webapp.RequestHandler):
    def get(self):
        rs = models.getAllBusinessEntities()
        path=os.path.join(os.path.dirname(__file__),'../view','dining.html')
        self.response.out.write(template.render(path, {"section_title":"Dining","beList":rs}))    

# Renders the DiningDetails page        
class DiningDetailsHandler(webapp.RequestHandler):
    def get(self):
        name = self.request.get('name')
        rs = models.getBusinessEntity(name)
        path=os.path.join(os.path.dirname(__file__),'../view','diningdetails.html')
        self.response.out.write(template.render(path, {"section_title":"Dining","be":rs}))    
         
if __name__ == '__main__':
    pass