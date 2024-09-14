package com.app.services.impl;

import com.app.config.AppConstants;
import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Customer;
import com.app.entites.Role;
import com.app.entites.type.AddressTypeConverter;
import com.app.exceptions.APIErrorCode;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CartDTO;
import com.app.payloads.CustomerDTO;
import com.app.payloads.SkuDTO;
import com.app.payloads.response.UserResponse;
import com.app.repositories.AddressRepo;
import com.app.repositories.CustomerRepo;
import com.app.repositories.RoleRepo;
import com.app.services.CartService;
import com.app.services.UserService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Transactional
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private CustomerRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private CartService cartService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    AddressTypeConverter addressTypeConverter;

    @Override
    public CustomerDTO registerUser(CustomerDTO customerReq) {

        try {
            String encodedPass = passwordEncoder.encode(customerReq.getPassword());
            customerReq.setPassword(encodedPass);
            Customer user = modelMapper.map(customerReq, Customer.class);

            Cart cart = new Cart();
            user.setCart(cart);

            Role role = roleRepo.findById(AppConstants.USER_ROLE_ID).get();
            user.getRoles().add(role);

            log.debug("(User delivery address \t {}", customerReq.getDeliveryAddress());
            if (customerReq.getDeliveryAddress() != null) {
                /*
                 * String country = userDTO.getAddress().getCountry(); String state =
                 * userDTO.getAddress().getState(); String city =
                 * userDTO.getAddress().getCity(); String pincode =
                 * userDTO.getAddress().getPincode(); String street =
                 * userDTO.getAddress().getAddress1(); String buildingName =
                 * userDTO.getAddress().getAddress2();
                 * 
                 * Address address =
                 * addressRepo.findByCountryAndStateAndCityAndPincodeAndAddress1AndAddress2(
                 * country, state, city, pincode, street, buildingName);
                 * 
                 * if (address == null) { address = new Address(country, state, city, pincode,
                 * street, buildingName);
                 * 
                 * address = addressRepo.save(address); }
                 */
                user.setDeliveryAddress(addressTypeConverter.toEntityType(customerReq.getDeliveryAddress()));
                // user.setAddresses(List.of(address));
            }
            cart.setUser(user);
            Customer registeredUser = userRepo.saveAndFlush(user);
            customerReq = modelMapper.map(registeredUser, CustomerDTO.class);
            // userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(),
            // AddressDTO.class));
            return customerReq;
        } catch (DataIntegrityViolationException e) {
            log.error("Error occured while creating user for user {}", customerReq.getEmail(), e);
            throw new APIException(APIErrorCode.API_417, e.getMessage() + customerReq.getEmail());
        }
    }

    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Customer> pageUsers = userRepo.findAll(pageDetails);

        List<Customer> users = pageUsers.getContent();

        if (users.size() == 0) {
            throw new APIException("No User exists !!!");
        }

        List<CustomerDTO> userDTOs = users.stream().map(user -> {
            CustomerDTO dto = modelMapper.map(user, CustomerDTO.class);
            /*
             * if (user.getAddresses().size() != 0) { dto.setAddress(modelMapper.map(
             * user.getAddresses().stream().findFirst().get(), AddressDTO.class)); }
             */
            CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class);
            List<SkuDTO> skuDTOs = user.getCart().getCartItems().stream()
                    .map(item -> modelMapper.map(item.getSku(), SkuDTO.class)).collect(Collectors.toList());
            dto.setCart(cart);
            // dto.getCart().setSkus(skuDTOs);
            return dto;
        }).collect(Collectors.toList());

        UserResponse userResponse = new UserResponse();
        userResponse.setContent(userDTOs);
        userResponse.setPageNumber(pageUsers.getNumber());
        userResponse.setPageSize(pageUsers.getSize());
        userResponse.setTotalElements(pageUsers.getTotalElements());
        userResponse.setTotalPages(pageUsers.getTotalPages());
        userResponse.setLastPage(pageUsers.isLast());
        return userResponse;
    }

    @Override
    public CustomerDTO getUserById(Long userId) {
        Customer user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        CustomerDTO userDTO = modelMapper.map(user, CustomerDTO.class);

        /*
         * userDTO.setAddress(
         * modelMapper.map(user.getAddresses().stream().findFirst().get(),
         * AddressDTO.class));
         */

        if (user.getCart() != null) {
            CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class);
            List<SkuDTO> skuDTOs = user.getCart().getCartItems().stream()
                    .map(item -> modelMapper.map(item.getSku(), SkuDTO.class)).collect(Collectors.toList());
            userDTO.setCart(cart);

            // userDTO.getCart().setSkus(skuDTOs);

        }

        return userDTO;
    }

    @Override
    public CustomerDTO updateUser(Long userId, CustomerDTO userDTO) {
        Customer user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        String encodedPass = passwordEncoder.encode(userDTO.getPassword());

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMobile(userDTO.getMobile());
        user.setEmail(userDTO.getEmail());
        user.setPassword(encodedPass);

        /*
         * if (userDTO.getAddress() != null) { String country =
         * userDTO.getAddress().getCountry(); String state =
         * userDTO.getAddress().getState(); String city =
         * userDTO.getAddress().getCity(); String pincode =
         * userDTO.getAddress().getPincode(); String street =
         * userDTO.getAddress().getAddress1(); String buildingName =
         * userDTO.getAddress().getAddress2();
         * 
         * Address address =
         * addressRepo.findByCountryAndStateAndCityAndPincodeAndAddress1AndAddress2(
         * country, state, city, pincode, street, buildingName);
         * 
         * if (address == null) { address = new Address(street, buildingName, city,
         * state, country, pincode); address = addressRepo.save(address);
         * user.setAddresses(List.of(address)); } }
         */

        userDTO = modelMapper.map(user, CustomerDTO.class);

        /*
         * userDTO.setAddress(
         * modelMapper.map(user.getAddresses().stream().findFirst().get(),
         * AddressDTO.class));
         */

        CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class);

        List<SkuDTO> skuDTOs = user.getCart().getCartItems().stream()
                .map(item -> modelMapper.map(item.getSku(), SkuDTO.class)).collect(Collectors.toList());
        userDTO.setCart(cart);

        // userDTO.getCart().setSkus(skuDTOs);
        return userDTO;
    }

    @Override
    public String deleteUser(Long userId) {
        Customer user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        List<CartItem> cartItems = user.getCart().getCartItems();
        Long cartId = user.getCart().getId();

        /*
         * cartItems.forEach(item -> {
         *
         * Long skuId = item.getSku().getSkuId();
         *
         * cartService.deleteProductFromCart(cartId, skuId); });
         */

        userRepo.delete(user);

        return "User with userId " + userId + " deleted successfully!!!";
    }
}
