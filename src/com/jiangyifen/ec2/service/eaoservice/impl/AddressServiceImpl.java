package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.eao.AddressEao;
import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.service.eaoservice.AddressService;

public class AddressServiceImpl implements AddressService{
	private AddressEao addressEao;
	// enhanced method 
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Address> loadPageEntities(int start, int length, String sql) {
		if ("".equals(sql.trim())) {
			return new ArrayList<Address>();
		}
		return addressEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		if ("".equals(sql.trim())) {
			return 0;
		}
		return addressEao.getEntityCount(sql);
	}
	
	// common method
	
	@Override
	public Address getAddress(Object primaryKey) {
		return addressEao.get(Address.class, primaryKey);
	}

	@Override
	public void saveAddress(Address address) {
		addressEao.save(address);
	}

	@Override
	public void updateAddress(Address Address) {
		addressEao.update(Address);
	}

	@Override
	public void deleteAddress(Address address) {
		addressEao.delete(address);
	}

	@Override
	public void deleteAddressById(Object primaryKey) {
		addressEao.delete(Address.class, primaryKey);
	}

	//Getter and Setter
	public AddressEao getAddressEao() {
		return addressEao;
	}

	public void setAddressEao(AddressEao addressEao) {
		this.addressEao = addressEao;
	}
	
}
