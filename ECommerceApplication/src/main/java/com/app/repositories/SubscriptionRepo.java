package com.app.repositories;

import com.app.entites.Subscription;
import com.app.entites.SubscriptionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {

    @Query("Select id,quantity,frequency,status,fromStartDate,nextDeliveryDate from Subscription s where s.id=?1")
    Optional<Subscription> findSubscription(final Long subId);
    
    Optional<Subscription> findSubscriptionByIdAndVendorId(final Long subscriptionId,final Long vendorId);
    
    List<Subscription> findByVendorIdAndStatus(final Long vendorId,SubscriptionStatus status);
}
