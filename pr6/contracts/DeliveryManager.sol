// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/// @title DeliveryManager
/// @notice Stores deliveries and enforces their lifecycle and role permissions.
contract DeliveryManager {
    enum DeliveryStatus {
        Created,
        Accepted,
        InTransit,
        Delivered,
        Cancelled
    }

    struct Delivery {
        uint256 deliveryId;
        uint256 orderId;
        address customer;
        address courier;
        uint256 price;
        DeliveryStatus status;
    }

    address public immutable operator;
    uint256 public deliveryCount;

    mapping(uint256 deliveryId => Delivery delivery) private deliveries;
    mapping(uint256 orderId => uint256 deliveryId) public deliveryIdByOrderId;

    error OnlyOperator(address caller);
    error DeliveryNotFound(uint256 deliveryId);
    error InvalidOrderId();
    error InvalidAddress();
    error InvalidPrice();
    error OrderNotFound(uint256 orderId);
    error OrderAlreadyRegistered(uint256 orderId, uint256 deliveryId);
    error CourierAssignmentClosed(uint256 deliveryId, DeliveryStatus currentStatus);
    error CourierNotAssigned(uint256 deliveryId);
    error OnlyAssignedCourier(address caller, address assignedCourier);
    error OnlyCustomerOrOperator(address caller);
    error InvalidStatusTransition(DeliveryStatus from, DeliveryStatus to);

    event DeliveryCreated(
        uint256 indexed deliveryId,
        uint256 indexed orderId,
        address indexed customer,
        uint256 price
    );
    event CourierAssigned(uint256 indexed deliveryId, address indexed courier);
    event DeliveryStatusChanged(
        uint256 indexed deliveryId,
        DeliveryStatus previousStatus,
        DeliveryStatus newStatus,
        address indexed changedBy
    );

    constructor() {
        operator = msg.sender;
    }

    modifier onlyOperator() {
        if (msg.sender != operator) {
            revert OnlyOperator(msg.sender);
        }
        _;
    }

    modifier existingDelivery(uint256 deliveryId) {
        if (deliveryId == 0 || deliveryId > deliveryCount) {
            revert DeliveryNotFound(deliveryId);
        }
        _;
    }

    modifier onlyAssignedCourier(uint256 deliveryId) {
        address assignedCourier = deliveries[deliveryId].courier;
        if (assignedCourier == address(0)) {
            revert CourierNotAssigned(deliveryId);
        }
        if (msg.sender != assignedCourier) {
            revert OnlyAssignedCourier(msg.sender, assignedCourier);
        }
        _;
    }

    /// @notice Creates a delivery in the Created state. Only the operator can call it.
    function createDelivery(
        uint256 orderId,
        address customer,
        uint256 price
    ) external onlyOperator returns (uint256 deliveryId) {
        if (orderId == 0) {
            revert InvalidOrderId();
        }
        if (customer == address(0)) {
            revert InvalidAddress();
        }
        if (price == 0) {
            revert InvalidPrice();
        }

        uint256 existingDeliveryId = deliveryIdByOrderId[orderId];
        if (existingDeliveryId != 0) {
            revert OrderAlreadyRegistered(orderId, existingDeliveryId);
        }

        deliveryId = ++deliveryCount;
        deliveries[deliveryId] = Delivery({
            deliveryId: deliveryId,
            orderId: orderId,
            customer: customer,
            courier: address(0),
            price: price,
            status: DeliveryStatus.Created
        });
        deliveryIdByOrderId[orderId] = deliveryId;

        emit DeliveryCreated(deliveryId, orderId, customer, price);
    }

    /// @notice Assigns or replaces a courier while the delivery is still in Created state.
    function assignCourier(
        uint256 deliveryId,
        address courier
    ) external onlyOperator existingDelivery(deliveryId) {
        if (courier == address(0)) {
            revert InvalidAddress();
        }

        Delivery storage delivery = deliveries[deliveryId];
        if (delivery.status != DeliveryStatus.Created) {
            revert CourierAssignmentClosed(deliveryId, delivery.status);
        }

        delivery.courier = courier;
        emit CourierAssigned(deliveryId, courier);
    }

    /// @notice Assigned courier accepts the delivery: Created -> Accepted.
    function acceptDelivery(
        uint256 deliveryId
    ) external existingDelivery(deliveryId) onlyAssignedCourier(deliveryId) {
        _changeStatus(deliveries[deliveryId], DeliveryStatus.Accepted);
    }

    /// @notice Assigned courier starts transportation: Accepted -> InTransit.
    function startDelivery(
        uint256 deliveryId
    ) external existingDelivery(deliveryId) onlyAssignedCourier(deliveryId) {
        _changeStatus(deliveries[deliveryId], DeliveryStatus.InTransit);
    }

    /// @notice Assigned courier completes transportation: InTransit -> Delivered.
    function completeDelivery(
        uint256 deliveryId
    ) external existingDelivery(deliveryId) onlyAssignedCourier(deliveryId) {
        _changeStatus(deliveries[deliveryId], DeliveryStatus.Delivered);
    }

    /// @notice Customer or operator can cancel before transportation has started.
    function cancelDelivery(
        uint256 deliveryId
    ) external existingDelivery(deliveryId) {
        Delivery storage delivery = deliveries[deliveryId];
        if (msg.sender != delivery.customer && msg.sender != operator) {
            revert OnlyCustomerOrOperator(msg.sender);
        }

        _changeStatus(delivery, DeliveryStatus.Cancelled);
    }

    function getDelivery(
        uint256 deliveryId
    ) external view existingDelivery(deliveryId) returns (Delivery memory) {
        return deliveries[deliveryId];
    }

    function getDeliveryByOrderId(
        uint256 orderId
    ) external view returns (Delivery memory) {
        uint256 deliveryId = deliveryIdByOrderId[orderId];
        if (deliveryId == 0) {
            revert OrderNotFound(orderId);
        }
        return deliveries[deliveryId];
    }

    /// @notice Human-readable status for a clearer Remix demonstration.
    function getStatusLabel(
        uint256 deliveryId
    ) external view existingDelivery(deliveryId) returns (string memory) {
        DeliveryStatus status = deliveries[deliveryId].status;

        if (status == DeliveryStatus.Created) return "Created";
        if (status == DeliveryStatus.Accepted) return "Accepted";
        if (status == DeliveryStatus.InTransit) return "InTransit";
        if (status == DeliveryStatus.Delivered) return "Delivered";
        return "Cancelled";
    }

    function _changeStatus(Delivery storage delivery, DeliveryStatus newStatus) private {
        DeliveryStatus previousStatus = delivery.status;

        bool allowed =
            (previousStatus == DeliveryStatus.Created &&
                (newStatus == DeliveryStatus.Accepted || newStatus == DeliveryStatus.Cancelled)) ||
            (previousStatus == DeliveryStatus.Accepted &&
                (newStatus == DeliveryStatus.InTransit || newStatus == DeliveryStatus.Cancelled)) ||
            (previousStatus == DeliveryStatus.InTransit && newStatus == DeliveryStatus.Delivered);

        if (!allowed) {
            revert InvalidStatusTransition(previousStatus, newStatus);
        }

        delivery.status = newStatus;
        emit DeliveryStatusChanged(
            delivery.deliveryId,
            previousStatus,
            newStatus,
            msg.sender
        );
    }
}
