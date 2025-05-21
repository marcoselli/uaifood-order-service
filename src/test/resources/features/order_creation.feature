Feature: Order Creation
  As a customer
  I want to create a new order
  So that I can receive my food delivery

  Scenario: Create a new order successfully
    Given I am a customer with ID "customer123"
    And I want to order from restaurant "restaurant456"
    When I add the following items to my order:
      | Product ID                           | Quantity | Price |
      | 550e8400-e29b-41d4-a716-446655440000 | 2        | 10.0  |
      | 6ba7b810-9dad-11d1-80b4-00c04fd430c8 | 1        | 15.0  |
    And I submit my order
    Then my order should be created successfully
    And the order status should be "CREATED"
    And the total amount should be 35.0
    And an order created event should be published

  Scenario: Create an order with no items
    Given I am a customer with ID "customer123"
    And I want to order from restaurant "restaurant456"
    When I submit my order without any items
    Then the order creation should fail
    And I should receive an error message about empty order 