- create control plan, 2 worker node: `kind create cluster --name demo --config kind-config.yaml`
- kubectl get nodes -o wide
![alt text](image.png)
- run ex app ex: `kubectl apply -f nginx-dev.yaml`
- ![alt text](image-1.png)
- Architect 
![alt text](image-2.png)
![alt text](image-3.png)
- woker increase scalability and high availability
- service port 
![alt text](image-4.png)
- nginx-dev.yaml file '---' must be 
- control plane routing request to worker in k8s ( on production add lb first )
    - config here
        ![alt text](image-5.png)
        ![alt text](image-6.png)
        ![alt text](image-7.png)
- k8s service port
    - ![alt text](image-9.png)


- LB approach 
![alt text](image-8.png)