package service.impl;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SparePartServiceImp implements SparePartService{
    @Autowired
    SparePartService sparePartService;
}
