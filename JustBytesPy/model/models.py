from google.appengine.ext import db
import os

class BusinessEntity(db.Model):
    businessId = db.IntegerProperty()
    name = db.StringProperty()
    type = db.IntegerProperty()  # RESTAURANT=1 or GROCERY=2
    address = db.PostalAddressProperty()
    picture = db.BlobProperty()
    geoLocation = db.GeoPtProperty()
    phoneNumber = db.PhoneNumberProperty()
    faxNumber = db.PhoneNumberProperty()
    email = db.EmailProperty()
    www = db.LinkProperty()
    fb = db.LinkProperty()
    description1 = db.StringProperty()
    description2 = db.StringProperty()
    activeIn = db.BooleanProperty()
    updateDtm = db.DateTimeProperty(auto_now=True)
    
    
def insertBe():
    testBe = BusinessEntity(businessId=1, name='Jaipur Indian Restaurant', type=1, address='5909 South BLVD, Charlotte, NC 28217', 
                            geoLocation=db.GeoPt(35.15493, -80.87506), phoneNumber=db.PhoneNumber("1 (704)-724-5555"),
                            email=db.Email("info@jaipur-indian.com"),www=db.Link("http://www.jaipur-indian.com"),activeIn=True)
    # to read local file, it shld not be mapped as static in app.yaml..so using icons
    path=os.path.join(os.path.dirname(__file__),'../asset','icons','jaipurindianclt.jpg')
    testBe.picture = db.Blob(open(path).read());
    db.put(testBe)
    
def getBusinessEntity(name):
    rs = db.GqlQuery("SELECT * FROM BusinessEntity WHERE name = :1", name).fetch(1)
    return rs[0]
    
def getAllBusinessEntities():
    rs = BusinessEntity.gql("where activeIn = True ")
    return rs

    
 
    
