package com.app.services.impl;

import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Customer;
import com.app.entites.Order;
import com.app.entites.OrderItem;
import com.app.entites.OrderStatusHistory;
import com.app.entites.Payment;
import com.app.entites.Shipping;
import com.app.entites.Vendor;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.OrderDTO;
import com.app.payloads.OrderRequest;
import com.app.payloads.OrderResponse;
import com.app.payloads.request.OrderUpdateRequest;
import com.app.payloads.response.AppResponse;
import com.app.payloads.response.OrderUpdateResponse;
import com.app.repositories.CartItemRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.CustomerRepo;
import com.app.repositories.OrderItemRepo;
import com.app.repositories.OrderRepo;
import com.app.repositories.OrderStatusRepo;
import com.app.repositories.PaymentRepo;
import com.app.repositories.VendorRepo;
import com.app.services.CartService;
import com.app.services.OrderService;
import com.app.services.UserService;
import com.app.services.constants.OrderStatus;
import com.app.services.constants.PaymentType;
import com.app.services.constants.ShippingType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl extends AbstarctCatalogService implements OrderService {

    @Autowired
    private OrderStatusRepo orderStatusRepo;

    public OrderServiceImpl(CustomerRepo userRepo, CartRepo cartRepo, OrderRepo orderRepo, PaymentRepo paymentRepo,
            OrderItemRepo orderItemRepo, CartItemRepo cartItemRepo, UserService userService, CartService cartService,
            VendorRepo storeRepo, ModelMapper modelMapper) {
        super(userRepo, cartRepo, orderRepo, paymentRepo, orderItemRepo, cartItemRepo, userService, cartService,
                storeRepo, modelMapper);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = {
            Exception.class, APIException.class, ResourceNotFoundException.class })
    @Override
    public AppResponse<OrderDTO> placeOrder(final Long storeId, final OrderRequest request) {
        final Long userId = request.getUserId();
        final Long cartId = request.getCartId();
        final Customer user = validateUser(userId);
        Cart cart = validateCart(cartId, user);
        final Vendor store = validateCartItemsAndStore(storeId, cart);
        Order order = createOrder(request, user, cart, store);
        processOrderItems(cart, order);
        // Update inventory
        updateCartAndSkuQuantities(cart);
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        order = orderRepo.saveAndFlush(order);
        orderDTO.setOrderId(order.getOrderId());
        return AppResponse.success(HttpStatus.OK.value(), orderDTO);
    }

    @Override
    public List<OrderDTO> getOrdersByUser(String emailId) {
        List<Order> orders = orderRepo.findAllByCustomerEmail(emailId);

        List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());

        if (orderDTOs.size() == 0) {
            throw new APIException("No orders placed yet by the user with email: " + emailId);
        }

        return orderDTOs;
    }

    @Override
    public OrderDTO getOrder(String emailId, Long orderId) {

        Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);

        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", orderId);
        }

        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Order> pageOrders = orderRepo.findAll(pageDetails);

        List<Order> orders = pageOrders.getContent();

        List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());

        if (orderDTOs.size() == 0) {
            throw new APIException("No orders placed yet by the users");
        }

        OrderResponse orderResponse = new OrderResponse();

        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(pageOrders.getNumber());
        orderResponse.setPageSize(pageOrders.getSize());
        orderResponse.setTotalElements(pageOrders.getTotalElements());
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setLastPage(pageOrders.isLast());

        return orderResponse;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public AppResponse<OrderUpdateResponse> updateOrder(Long orderId, OrderUpdateRequest request) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", orderId);
        }
        switch (order.getOrderStatus()) {
        case DELIVERED, CANCELED -> {
            throw new APIException("Invalid Order Status..");
        }
        }
        ;
        // OrderStatus newOrderStatus = OrderStatus.valueOf(request.getNewSatus());

        OrderStatusHistory orderStatusHistory = createOrderStatusHistory(order);
        orderStatusHistory.setOldStatus(order.getOrderStatus());
        orderStatusHistory.setNewStatus(request.getNewSatus());
        order.setOrderStatus(request.getNewSatus());
        orderStatusRepo.save(orderStatusHistory);
        return AppResponse.success(HttpStatus.OK.value(),
                modelMapper.map(OrderStatusHistory.class, OrderUpdateResponse.class));
    }

    @Override
    public OrderDTO updateOrder(String emailId, Long orderId, OrderStatus orderStatus) {
        Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", orderId);
        }
        order.setOrderStatus(orderStatus);
        return modelMapper.map(order, OrderDTO.class);
    }

    /**
     * Create and populate the Order entity
     *
     * @param storeId
     * @param request
     * @param user
     * @param cart
     * @return
     */
    private Order createOrder(OrderRequest request, Customer user, Cart cart, Vendor store) {
        Order order = new Order();
        // order.setEmail(user.getEmail());
        // order.setOrderTime(Instant.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setFederalTax(cart.getTotalPrice() * 0.2);
        order.setStateTax(cart.getTotalPrice() * 0.5);
        order.setSubTotal(cart.getTotalPrice());
        order.setTotalAmount(cart.getTotalPrice() + order.getFederalTax() + order.getStateTax());

        order.setCustomer(user);
        order.setVendor(store);
        createPayment(request, order);
        createShipping(request, order);
        return order;
    }

    private void processOrderItems(Cart cart, Order order) {
        List<OrderItem> orderItems = cart.getCartItems().stream().map(cartItem -> createOrderItem(cartItem, order))
                .collect(Collectors.toList());
        order.setItems(orderItems);
    }

    private OrderItem createOrderItem(CartItem cartItem, Order order) {
        // Create and populate the OrderItem entity
        OrderItem orderItem = new OrderItem();
        orderItem.setSku(cartItem.getSku());
        orderItem.setUnitPrice(cartItem.getUnitPrice());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setAmount(cartItem.getQuantity() * cartItem.getUnitPrice());
        orderItem.setDiscount(cartItem.getDiscount());
        orderItem.setOrder(order);
        return orderItem;
    }

    private void createPayment(final OrderRequest request, Order order) {
        Payment payment = new Payment();
        payment = modelMapper.map(request.getPaymentDetails(), Payment.class);
        payment.setOrder(order);
        PaymentType paymentType = PaymentType.valueOf(request.getPaymentDetails().getPaymentMethod());
        payment.setPaymentMethod(paymentType);
        // payment = paymentRepo.save(payment);
        order.setPayment(payment);
    }

    private void createShipping(final OrderRequest request, Order order) {
        Shipping shipping = modelMapper.map(request.getShippingDetails(), Shipping.class);
        ShippingType shippingType = ShippingType.valueOf(request.getShippingDetails().getShippingMethod());
        shipping.setShippingMethod(shippingType);
        shipping.setOrder(order);
        // Shipping
        order.setShipping(shipping);
    }

    private OrderStatusHistory createOrderStatusHistory(Order order) {
        OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
        orderStatusHistory.setOrder(order);
        orderStatusHistory.setOldStatus(order.getOrderStatus());
        orderStatusHistory.setNewStatus(OrderStatus.PENDING);
        orderStatusHistory.setChangedAt(LocalDateTime.now());
        return orderStatusHistory;
    }

    @Override
    public AppResponse<OrderDTO> getOrderById(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        try {
            modelMapper.map(order, OrderDTO.class);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return AppResponse.success(HttpStatus.OK.value(), modelMapper.map(order, OrderDTO.class));
    }

    @Override
    public AppResponse<List<OrderDTO>> getOrderByStoreId(Long storeId) {
        List<OrderDTO> orders = orderRepo.findOrderByVendorId(storeId).stream()
                .map(orderEntity -> modelMapper.map(orderEntity, OrderDTO.class)).collect(Collectors.toList());
        return AppResponse.success(HttpStatus.OK.value(), orders);
    }
}
