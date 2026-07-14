# Performance Testing

Tool Used:
- Apache JMeter 5.6.3

Configuration:
- Threads (Users): 200
- Ramp-up: 10 seconds
- Loop Count: 10
- Total Requests: 2000

API Tested:
GET / api/preferences

Results:
- Average Response Time: 18 ms
- Maximum Response Time: 95 ms
- Error Rate: 0%

Conclusion:
The API successfully responds within 200 ms under normal load.