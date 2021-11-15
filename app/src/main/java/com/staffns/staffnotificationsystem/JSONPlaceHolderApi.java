package com.staffns.staffnotificationsystem;

        import retrofit2.Call;
        import retrofit2.http.GET;
        import retrofit2.http.Query;

public interface JSONPlaceHolderApi {

    @GET("/receiving_the_information.php")
    Call<Order> automaticDataUpdate(@Query("login") String login, @Query("pass") String pass, @Query("X") double X, @Query("Y") double Y, @Query("token") String token);

    @GET("/full_name.php")
    Call<Order> getFull_name(@Query("login") String login, @Query("pass") String pass);

    @GET("/complete_order.php")
    Call<Order> completeOrder(@Query("login") String login, @Query("pass") String pass, @Query("id") int id);
}