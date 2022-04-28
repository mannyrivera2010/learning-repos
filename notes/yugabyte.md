
```
import redis

# Create the cluster connection.
r = redis.Redis(host='localhost', port=6379)

# Insert the user profile.
userid = 1
user_profile = {"name": "John", "age": "35", "language": "Python"}
r.hmset(userid, user_profile)
print "Inserted userid=1, profile=%s" % user_profile

# Query the user profile.
print r.hgetall(userid)
```

https://blog.yugabyte.com/building-scalable-cloud-services-an-instant-messaging-app-97cd52fbc121
https://docs.yugabyte.com/latest/develop/client-drivers/python/#redis
https://github.com/YugaByte/yugastore/blob/master/routes/products.js


```

/* Return details of a specific product id. */
router.get('/details/:id', function(req, res, next) {
  var redisKeyPrefix = 'pageviews:product:' + req.params.id + ':';

  // Increment the num pageviews for the product.
  ybRedis.incrby(redisKeyPrefix + "count", 1);
  console.log("Responding for id: " + req.params.id);

  // Track the history of pageviews for the product.
  var payload = "{ userid: '12345', referral_source: 'google', referral_url: 'xyz' }"
  var d = new Date();
  var timestamp = Math.round(d.getTime() / 1000);
  ybRedis.zadd(redisKeyPrefix + "history", timestamp, payload);

  // Return the product details.
  var productDetails = {}
  var selectStmt = 'SELECT * FROM yugastore.products WHERE id=' + req.params.id + ';';
  ybCassandra.execute(selectStmt)
              .then(result => {
                var row = result.first();
                var avgStars = row.total_stars / row.num_reviews;
                row.stars = avgStars.toFixed(2);                
                productDetails = Object.assign({}, row);
                return res.json(productDetails);
              });
});
```
