# Mulit Tenant Subscription Model Application

- The Multi Tenant E-Commerce Application is built using Java 21 and Spring Boot3.2.1, with security, scalability, and ease of maintenance. The backend uses Spring Data JPA to interact with a PostgraSQL database, making it easy to manage and store important entities such as stores, users, products, categories, orders, and more. User authentication is handled by Auth0, providing secure and reliable means of REST APIs.

- The APIs are well-documented and easily accessible through Swagger UI, making it simple for developers to test and understand the various endpoints. Overall, this project provides secure Rest APIs to create a scalable platform for businesses to sell their products to customers.

# Features

| Vendor Admin |  Customer  | 
|:-----|:--------:|
| Catalog Management   |  SignUp & SignIn |
| Prommotions  |  Catalog Browse  |
| New Product Notifications  | Subscription Management |
| Order Updates| Order History|
|Promotions|Account History|
|Daily Reports| - |

# Security
- Stateless SEssion Management
- The API is secured using JSON Web Tokens (JWT) handled by Auth0. To access the API, you will need to obtain a JWT by authenticating with the /login endpoint. The JWT should then be passed in the Authorize option available in the Swagger-ui.
- Access Token (Short Lived TOken)
- Refresh Token (Long Lived Token)
- Cusomer Roles
- Vendor Roles

  ### Example:
  - Authorization: <your_jwt>

# Technologies:
- Java 21
- Spring Boot 3.2.1
- Maven
- PostgraSQL
- Spring Data JPA
- Spring Security
- JSON Web Tokens (JWT)
- Auth0
- Swagger UI
- Event Listner

# Running the app
1. Clone the repository: git clone https://github.com/s713278/ECommerceApp.git 
2. Import the project into STS:
  - Click File > Import...
  - Select Maven > Existing Maven Projects and click Next
  - Browse to the project directory and click Finish
3. Update the values in application.properties with your MySQL database connection details.
4. Run the app: Right-click the project in the Package Explorer and click Run As > Spring Boot App.

# API documentation
- API documentation is available via Swagger UI at http://localhost:8080/swagger-ui/index.html

# ER-Diagram
![multi_ecommerce - public](https://github.com/user-attachments/assets/c7ef19b4-fe5d-4318-b277-40f4b4776cca)

# Swagger-ui
<img width="947" alt="Swagger-UI" src="https://user-images.githubusercontent.com/101395494/216388614-f8eed33e-cbbb-4cfa-997e-b76674bbb465.png">

# Rest Services
![image](https://github.com/s713278/ECommerceApp/assets/14287419/a9188676-3211-4eb3-8a3d-f13ed18536d7)
![image](https://github.com/s713278/ECommerceApp/assets/14287419/ead9e229-37aa-4ee2-995d-15db39d9264a)
![image](https://github.com/s713278/ECommerceApp/assets/14287419/c01491ba-ef2e-48ed-8ef6-7b019cb0965e)
![image](https://github.com/s713278/ECommerceApp/assets/14287419/5c279941-59dd-492d-b7a1-067edd5a166b)

# Thank You
