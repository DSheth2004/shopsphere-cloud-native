# Demo script

1. Start `docker compose up --build`.
2. Register/login, create a product, set inventory, and create an order through port 8080.
3. Set `PAYMENT_FAILURE_RATE=1` and show cancellation/stock compensation.
4. Deploy to kind, delete a gateway pod, and show replacement.
5. Run k6 and show HPA/Prometheus evidence.
