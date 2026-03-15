USE javadb;

-- Tạo View để quản lý dễ dàng trong Java:
CREATE VIEW LowStockWarning AS
SELECT product_id, product_name, stock_quantity, price
FROM Products
WHERE stock_quantity <= 5 AND status != 'Discontinued';

-- Stored procedure cập nhật kho hàng:
DELIMITER //
CREATE PROCEDURE UpdateProductStock(
    IN p_id INT, 
    IN p_change_amount INT
)
BEGIN
    DECLARE current_stock INT;
    
    -- Lấy số lượng hiện tại
    SELECT stock_quantity INTO current_stock FROM Products WHERE product_id = p_id;
    
    -- Kiểm tra nếu trừ kho mà kết quả < 0 thì báo lỗi
    IF (current_stock + p_change_amount) < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Lỗi: Số lượng trong kho không đủ để thực hiện giao dịch!';
    ELSE
        UPDATE Products 
        SET stock_quantity = stock_quantity + p_change_amount,
            status = IF(stock_quantity + p_change_amount = 0, 'Out of Stock', 'Available')
        WHERE product_id = p_id;
    END IF;
END //
DELIMITER ;

-- Thống kê sản phẩm bán chạy:
SELECT p.product_name, SUM(od.quantity) as total_sold
FROM OrderDetails od
JOIN Products p ON od.product_id = p.product_id
GROUP BY p.product_id
ORDER BY total_sold DESC
LIMIT 10;

-- Cập nhật trạng thái đơn hàng:
 UPDATE Orders 
SET order_status = 'Shipped' 
WHERE order_id = ? AND payment_status = 'Paid';
