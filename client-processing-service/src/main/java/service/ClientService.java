package service;

import com.t1.starter.pack.aop.annotations.LogDatasourceError;
import entity.Client;
import repository.ClientRepository;
import utils.ClientIdGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ClientIdGenerator clientIdGenerator;

    public ClientService(ClientRepository clientRepository, ClientIdGenerator clientIdGenerator) {
        this.clientRepository = clientRepository;
        this.clientIdGenerator = clientIdGenerator;
    }

    @LogDatasourceError
    public Client createClient(Client client) {
        if (client.getClientId() == null) {
            client.setClientId(clientIdGenerator.generateClientId());
        }

        if (clientRepository.existsByClientId(client.getClientId())) {
            throw new IllegalArgumentException("Клиент с id уже существует: " + client.getClientId());
        }

        return clientRepository.save(client);
    }

    @LogDatasourceError
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    @LogDatasourceError
    public Optional<Client> getClientByClientId(String clientId) {
        return clientRepository.findByClientId(clientId);
    }

    @LogDatasourceError
    public Optional<Client> getClientByUserId(Long userId) {
        return clientRepository.findByUserId(userId);
    }

    @LogDatasourceError
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @LogDatasourceError
    public Client updateClient(Long id, Client clientDetails) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Клиент с id не найден: " + id));

        if (!client.getClientId().equals(clientDetails.getClientId()) &&
                clientRepository.existsByClientId(clientDetails.getClientId())) {
            throw new IllegalArgumentException("Клиент с id уже существует: " + clientDetails.getClientId());
        }

        client.setFirstName(clientDetails.getFirstName());
        client.setMiddleName(clientDetails.getMiddleName());
        client.setLastName(clientDetails.getLastName());
        client.setDateOfBirth(clientDetails.getDateOfBirth());
        client.setDocumentType(clientDetails.getDocumentType());
        client.setDocumentId(clientDetails.getDocumentId());
        client.setDocumentPrefix(clientDetails.getDocumentPrefix());
        client.setDocumentSuffix(clientDetails.getDocumentSuffix());

        return clientRepository.save(client);
    }

    @LogDatasourceError
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new IllegalArgumentException("Клиент с id не найден: " + id);
        }
        clientRepository.deleteById(id);
    }
}
