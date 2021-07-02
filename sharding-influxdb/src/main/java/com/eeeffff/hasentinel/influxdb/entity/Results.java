package com.eeeffff.hasentinel.influxdb.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
@Data
public class Results {
	private int statement_id;
	private List<Series> series = new ArrayList<Series>();
}
