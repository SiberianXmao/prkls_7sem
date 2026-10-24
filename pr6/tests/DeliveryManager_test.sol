// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "remix_tests.sol";
import "../contracts/DeliveryManager.sol";
import "../contracts/DeliveryActor.sol";

contract DeliveryManagerTest {
    DeliveryManager private manager;
    DeliveryActor private customer;
    DeliveryActor private courier;
    DeliveryActor private outsider;

    function beforeEach() public {
        manager = new DeliveryManager();
        customer = new DeliveryActor();
        courier = new DeliveryActor();
        outsider = new DeliveryActor();

        manager.createDelivery(1001, address(customer), 125_000);
        manager.assignCourier(1, address(courier));
    }

    function testCreationAndAssignment() public {
        DeliveryManager.Delivery memory delivery = manager.getDelivery(1);

        Assert.equal(delivery.deliveryId, uint256(1), "delivery id must be generated");
        Assert.equal(delivery.orderId, uint256(1001), "order id must be stored");
        Assert.equal(delivery.customer, address(customer), "customer must be stored");
        Assert.equal(delivery.courier, address(courier), "courier must be assigned");
        Assert.equal(delivery.price, uint256(125_000), "price must be stored");
        Assert.equal(
            uint256(delivery.status),
            uint256(DeliveryManager.DeliveryStatus.Created),
            "initial status must be Created"
        );
    }

    function testCompleteLifecycle() public {
        courier.accept(manager, 1);
        courier.start(manager, 1);
        courier.complete(manager, 1);

        DeliveryManager.Delivery memory delivery = manager.getDelivery(1);
        Assert.equal(
            uint256(delivery.status),
            uint256(DeliveryManager.DeliveryStatus.Delivered),
            "delivery must reach Delivered"
        );
    }

    function testDirectCreatedToDeliveredIsRejected() public {
        (bool success, ) = address(courier).call(
            abi.encodeCall(DeliveryActor.complete, (manager, uint256(1)))
        );

        Assert.ok(!success, "Created to Delivered must be rejected");
        Assert.equal(
            uint256(manager.getDelivery(1).status),
            uint256(DeliveryManager.DeliveryStatus.Created),
            "status must remain Created"
        );
    }

    function testOnlyAssignedCourierCanAccept() public {
        (bool success, ) = address(outsider).call(
            abi.encodeCall(DeliveryActor.accept, (manager, uint256(1)))
        );

        Assert.ok(!success, "an outsider must not accept the delivery");
    }

    function testCustomerCanCancelBeforeTransit() public {
        courier.accept(manager, 1);
        customer.cancel(manager, 1);

        Assert.equal(
            uint256(manager.getDelivery(1).status),
            uint256(DeliveryManager.DeliveryStatus.Cancelled),
            "customer must be able to cancel before transit"
        );
    }

    function testCancellationAfterTransitIsRejected() public {
        courier.accept(manager, 1);
        courier.start(manager, 1);

        (bool success, ) = address(customer).call(
            abi.encodeCall(DeliveryActor.cancel, (manager, uint256(1)))
        );

        Assert.ok(!success, "cancellation after transit starts must be rejected");
        Assert.equal(
            uint256(manager.getDelivery(1).status),
            uint256(DeliveryManager.DeliveryStatus.InTransit),
            "status must remain InTransit"
        );
    }
}
