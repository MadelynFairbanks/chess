package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import request.RegisterRequest;
import result.RegisterResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegisterHandler implements Route {
    private final UserService service;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

            if (request == null ||
                    request.username() == null || request.username().isBlank() ||
                    request.password() == null || request.password().isBlank() ||
                    request.email() == null || request.email().isBlank()) {
                return JsonResponse.error(res, "bad request");
            }

            UserData user = new UserData(request.username(), request.password(), request.email());
            AuthData auth = service.register(user);
            RegisterResult result = new RegisterResult(auth.username(), auth.authToken());

            return JsonResponse.success(res, 200, result);
        } catch (DataAccessException e) {
            return JsonResponse.error(res, e.getMessage());
        } catch (Exception e) {
            return JsonResponse.exception(res, e);
        }
    }

}
