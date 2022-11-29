package com.kinx.batchprogram.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OneDto {
	private String one;
	
	@Override
	public String toString() {
		return one+"으쌰으쌰";
	}
}
