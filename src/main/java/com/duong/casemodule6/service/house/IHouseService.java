package com.duong.casemodule6.service.house;

import com.duong.casemodule6.entity.dto.nativequery.IAvailableForRentHouse;
import com.duong.casemodule6.entity.house.House;
import com.duong.casemodule6.service.IGerneralService;

public interface IHouseService extends IGerneralService<House> {
    Iterable<IAvailableForRentHouse> getListAvailableForRentHouse();
}
