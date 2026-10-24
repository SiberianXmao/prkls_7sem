// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "./DeliveryManager.sol";

/// @dev Test helper that makes calls to DeliveryManager from a separate address.
contract DeliveryActor {
    function accept(DeliveryManager manager, uint256 deliveryId) external {
        manager.acceptDelivery(deliveryId);
    }

    function start(DeliveryManager manager, uint256 deliveryId) external {
        manager.startDelivery(deliveryId);
    }

    function complete(DeliveryManager manager, uint256 deliveryId) external {
        manager.completeDelivery(deliveryId);
    }

    function cancel(DeliveryManager manager, uint256 deliveryId) external {
        manager.cancelDelivery(deliveryId);
    }
}
