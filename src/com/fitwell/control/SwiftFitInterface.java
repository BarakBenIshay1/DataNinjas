package com.fitwell.control;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fitwell.control.EquipmentImportController.EquipmentImportData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SwiftFitInterface {

	public EquipmentImportData fetchUpdates(Path path) {

		String sampleJson = """
				{
				  "monthlyUpdateDate": "2025-12-01",
				  "equipment": [
				    {
				      "serialNumber":"BNCH6001",
				      "equipmentTypeId": 6,
				      "quantity": 3,
				      "photoLinks": ["https://swiftfit.com/images/dumbbells.jpg"]
				    },
				    {
				      "serialNumber":"DMB8001",
				      "equipmentTypeId": 8,
				      "quantity": 6,
				      "photoLinks": ["https://swiftfit.com/images/treadmill.jpg"]
				    },
				    {
				      "serialNumber":"ROW3001",
				      "equipmentTypeId": 3,
				      "quantity": 5,
				      "photoLinks": ["https://swiftfit.com/images/yogamat.jpg"]
				    },
				    {
				      "serialNumber":"BOS15001",
				      "typeId": 15,
				      "quantity": 10,
				      "photoLinks": ["https://swiftfit.com/images/smithmachine.jpg"]
				    }
				  ]
				}
				""";

		Gson gson = new Gson();
		EquipmentImportData data = null;
		Type type = new TypeToken<EquipmentImportData>() {
		}.getType();

		try (Reader reader = Files.newBufferedReader(Path.of(FileHelper.getDataPath("equipment_updates.json")))) {
			data = gson.fromJson(reader, type);
		} catch (IOException e) {

			e.printStackTrace();
		}
		return data;
	}
}