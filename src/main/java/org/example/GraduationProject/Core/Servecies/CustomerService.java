package org.example.GraduationProject.Core.Servecies;

import org.example.GraduationProject.Common.DTOs.PaginationDTO;
import org.example.GraduationProject.Common.Entities.Customer;
import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Core.Repsitories.CustomerRepository;
import org.example.GraduationProject.Core.Repsitories.HallRepository;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private HallRepository hallRepository;
    public void addFavoriteHall(Long userId, Hall hall) throws UserNotFoundException {
        Customer customer = customerRepository.findByUserId(userId);
        Hall hall1 = hallRepository.findById(hall.getId()).orElseThrow(
                () -> new UserNotFoundException("Hall not found")
        );
        if(customer.getFavoriteHalls().contains(hall1)){
            throw new UserNotFoundException("Hall already exists in favorites");
        }
            customer.getFavoriteHalls().add(hall1);
            customerRepository.save(customer);

    }

    public void removeFavoriteHall(Long userId, Hall hall) throws UserNotFoundException {
        Customer customer = customerRepository.findByUserId(userId);
        Hall hall1 = hallRepository.findById(hall.getId()).orElseThrow(
                () -> new UserNotFoundException("Hall not found")
        );
        if (customer != null) {
            customer.getFavoriteHalls().remove(hall1);
            customerRepository.save(customer);
        }
    }

    public PaginationDTO<Hall> getFavoriteHalls(Long userId, int page, int size) throws UserNotFoundException {
        Customer customer = customerRepository.findByUserId(userId);
        if (customer == null) {
            throw new UserNotFoundException("Customer not found");
        }

        if (page < 1) {
            page = 1;
        }

        List<Hall> favoriteHalls = new ArrayList<>(customer.getFavoriteHalls());
        int totalHalls = favoriteHalls.size();
        int startIndex = Math.min((page - 1) * size, totalHalls);
        int endIndex = Math.min(startIndex + size, totalHalls);
        List<Hall> paginatedHalls = favoriteHalls.subList(startIndex, endIndex);

        PaginationDTO<Hall> paginationDTO = new PaginationDTO<>();
        paginationDTO.setTotalElements((long) totalHalls);
        paginationDTO.setTotalPages((int) Math.ceil((double) totalHalls / size));
        paginationDTO.setSize(size);
        paginationDTO.setNumber(page);
        paginationDTO.setNumberOfElements(paginatedHalls.size());
        paginationDTO.setContent(paginatedHalls);

        return paginationDTO;
    }

}
