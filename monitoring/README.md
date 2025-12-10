# Monitoring

Prometheus + Grafana setup for the SuppleMart backend.

## Setup

```bash
cd monitoring
docker-compose up -d
```

**URLs:**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001 (login: admin / admin)

To stop: `docker-compose down`

## Custom Metrics

The app exposes these business metrics at `/actuator/prometheus`:

**Gauges:**
- `supplemart_users` - registered users count
- `supplemart_products` - products in catalog
- `supplemart_orders_count` - total orders
- `supplemart_carts` - shopping carts
- `supplemart_products_low_stock` - products with stock < 10

**Counters:**
- `supplemart_orders_total` - orders created
- `supplemart_payments_total` - payments (tagged by status)
- `supplemart_user_registrations_total` - new registrations
- `supplemart_oauth2_logins_total` - OAuth2 logins

## Dashboard

Grafana comes with a pre-configured dashboard that shows:
- CPU and JVM heap usage
- User/Product/Order counts
- HTTP request rates and response times
- JVM memory over time

## Troubleshooting

**Prometheus not scraping?**
- Make sure the backend is running on port 8080
- Check http://localhost:9090/targets for errors

**Grafana showing "No Data"?**
- Wait a minute for metrics to populate
- Check if Prometheus datasource is connected
