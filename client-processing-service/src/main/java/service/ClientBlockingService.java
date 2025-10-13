package service;

import com.t1.starter.pack.aop.annotations.LogDatasourceError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClientBlockingService {
    @Autowired
    private UserService userService;

    @Autowired
    private ClientService clientService;

    @LogDatasourceError
    public void blockClientByClientId(String clientId) {
        clientService.getClientByClientId(clientId)
                .ifPresent(client -> {
                    userService.blockClient(client.getUser().getId());
                });
    }

    @LogDatasourceError
    public void blockClientByUserId(Long userId) {
        userService.blockClient(userId);
    }

    @LogDatasourceError
    public void unblockClientByClientId(String clientId) {
        clientService.getClientByClientId(clientId)
                .ifPresent(client -> {
                    userService.unblockClient(client.getUser().getId());
                });
    }

    @LogDatasourceError
    public void unblockClientByUserId(Long userId) {
        userService.unblockClient(userId);
    }
}
