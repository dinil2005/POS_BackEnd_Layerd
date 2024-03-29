package lk.ijse.pos_back_end.dao.custom.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.pos_back_end.dao.custom.OrderDAO;
import lk.ijse.pos_back_end.dto.OrderDTO;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderDAOImpl implements OrderDAO {
    private static Logger logger = LoggerFactory.getLogger(OrderDAOImpl.class);

    String GET_ALL_ORDERS = "SELECT * FROM order_details";
    String SAVE_ORDER_DETAILS = "INSERT INTO order_details(order_id,customer_id,date) VALUES (?,?,?)";
    String PLACE_ORDER = "INSERT INTO orders (order_id,item_name,qty,total) VALUES (?,?,?,?)";
    String DELETE_ORDER = "DELETE FROM order_details WHERE order_id = ?";

    @Override
    public String getAll(Connection connection, HttpServletResponse resp) {
        resp.setContentType("application/json"); // Set content type to JSON

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_ORDERS);
            ResultSet resultSet = preparedStatement.executeQuery();

            ArrayList<OrderDTO> getAllOrders = new ArrayList<>();

            while (resultSet.next()){
                OrderDTO orderDTO = new OrderDTO();

                orderDTO.setOrder_Id(resultSet.getString(1));
                orderDTO.setCustomer_Id(resultSet.getString(2));
                orderDTO.setDate(resultSet.getString(3));

                getAllOrders.add(orderDTO);
            }

            // Convert the list of customers to JSON using Jackson ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            String getAllOrders_Json = objectMapper.writeValueAsString(getAllOrders);
            return getAllOrders_Json;

        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SneakyThrows
    @Override
    public void save(OrderDTO dto, Connection connection) {


        try {
            // Assuming 'connection' is your database connection
            connection.setAutoCommit(false);

            // Save order details
            PreparedStatement preparedStatement = connection.prepareStatement(SAVE_ORDER_DETAILS);
            preparedStatement.setString(1, dto.getOrder_Id());
            preparedStatement.setString(2, dto.getCustomer_Id());
            preparedStatement.setString(3, dto.getDate());

            if (preparedStatement.executeUpdate() != 0) {
                logger.info("Order details saved");

                // Place order
                PreparedStatement preparedStatement1 = connection.prepareStatement(PLACE_ORDER);
                preparedStatement1.setString(1, dto.getOrder_Id());
                preparedStatement1.setString(2, dto.getItem_Name());
                preparedStatement1.setInt(3, dto.getQty());
                preparedStatement1.setDouble(4, dto.getTotal());

                if (preparedStatement1.executeUpdate() != 0) {
                    logger.info("Order placed");

                    // If everything is successful, commit the transaction
                    connection.commit();
                } else {
                    logger.info("Failed to place order");
                    // If placing the order fails, roll back the transaction
                    connection.rollback();
                }
            } else {
                logger.info("Failed to save order details");
            }
        } catch (SQLException e) {
            // Handle exceptions
            e.printStackTrace();
        } finally {
            // Always close resources in the 'finally' block
            try {
                if (connection != null) {
                    connection.setAutoCommit(true); // Reset auto-commit to true

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }




    }

    @Override
    public void update(OrderDTO dto, Connection connection) {

    }

    @Override
    public void delete(OrderDTO dto, Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_ORDER);
            preparedStatement.setString(1,dto.getOrder_Id());

            if (preparedStatement.executeUpdate() !=0){
                logger.info("Delete");
            }else{
                logger.info("Not Delete");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
