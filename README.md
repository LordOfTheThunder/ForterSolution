# Exercise Solution

## Running the Application
This is a standard Java Gradle application, so running a gradle bootRun will suffice to execute and test locally.
Additionally, you can run it with docker if you pull from the following dockerhub repo: https://hub.docker.com/r/mijux/forter-demo-image
The full command set is:
```
docker pull mijux/forter-demo-image
docker run -p 8080:8080 mijux/forter-demo-image
```

## Testing
GET request example:
`localhost:8080/api/v1/cryptocurrencies?op=compareToPast&currencies=BTC,ETH,DOGE&pastDate=2022-01-01`

### Efficiency

In terms of efficient implementation - I only did it as efficient as I could using the existing API on CryptoCompare. I did not do any in-memory caching (or some other solution like Redis).
Also I did not plan for scale (was not a requirement though)

### Post vs Get

The more correct approach was to model this as a Post due to constraints and security issues such as URL max length, URLs being stored in server logs (allowing hackers to learn user behavior) and an issue with client side caching.
In this exercise, to make it easy on the reading/testing and since I'm assuming the data is not sensitive and there are no "shared browser" issues I'm using Get.

### Security

In the real world, I would plan data sanitization on the input to avoid exploits such as SQL injections.
Since this solution only pipes the data to a secure CryptoCompare API I am assuming that they do the heavy lifting for me in terms of data sanitization, as my service does not have any additional business logic.
