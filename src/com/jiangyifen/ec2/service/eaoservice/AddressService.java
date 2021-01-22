package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface AddressService extends FlipSupportService<Address> {
	
	@Transactional
	public Address getAddress(Object primaryKey);
	
	@Transactional
	public void saveAddress(Address address);
	
	@Transactional
	public void updateAddress(Address address);
	
	@Transactional
	public void deleteAddress(Address address);

	@Transactional
	public void deleteAddressById(Object primaryKey);

}
