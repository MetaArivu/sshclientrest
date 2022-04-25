/**
 * (C) Copyright 2021 Araf Karsh Hamid 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fusion.air.microservice.adapters.controllers;

import io.fusion.air.microservice.domain.core.CommandResults;
import io.fusion.air.microservice.domain.core.SSHClient;
import io.fusion.air.microservice.domain.models.PaymentDetails;
import io.fusion.air.microservice.domain.models.PaymentStatus;
import io.fusion.air.microservice.domain.models.PaymentType;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.server.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * App Controller for the Service
 * 
 * @author arafkarsh
 * @version 1.0
 * 
 */
@Configuration
@RestController
// "/api/v1/payments"
@RequestMapping("${service.api.path}")
@RequestScope
@Tag(name = "Mock Service", description = "Ex. io.f.a.m.adapters.controllers.AppControllerImpl")
public class AppControllerImpl extends AbstractController {

	// Set Logger -> Lookup will automatically determine the class name.
	private static final Logger log = getLogger(lookup().lookupClass());
	
	@Autowired
	private ServiceConfiguration serviceConfig;
	private String serviceName;

	/**
	 * Command Status
	 * 
	 * @return
	 */
    @Operation(summary = "Check the Command status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
            description = "Command Status Check",
            content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
            description = "Invalid Command Reference No.",
            content = @Content)
    })
	@GetMapping("/status/{referenceNo}")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> getStatus(@PathVariable("referenceNo") String _referenceNo,
														HttpServletRequest request) throws Exception {
		log.info("|"+name()+"|Request to Command Status of Service... ");
		HashMap<String,Object> status = new HashMap<String,Object>();
		status.put("Code", 404);
		status.put("Status", true);
		status.put("ReferenceNo", _referenceNo);
		status.put("Message","Invalid Command Reference No.!");
		return ResponseEntity.ok(status);
	}

	/**
	 * Execute SSH Client Command
	 */
    @Operation(summary = "Execute SSH Client Command")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
            description = "Execute SSH Client Command",
            content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
            description = "Unable to Execute SSH Command",
            content = @Content)
    })
    @PostMapping("/command/{command}")
    public ResponseEntity<CommandResults> executeCommand(@PathVariable("command") String _command) {
		log.info("|"+name()+"|Request to execute command = "+_command);
		SSHClient cl = new SSHClient("demo", "password", "test.rebex.net", 22, 10);
		try {
			cl.clientStart();
			String result = cl.executeCommand(_command);
			cl.clientStop();
		} catch (IOException e) {
			e.printStackTrace();
		}
		CommandResults cr = cl.getResultArray().get(0);
		return ResponseEntity.ok(cr);
    }

	/**
	 * Execute SSH Client Commands (Multiple)
	 */
	@Operation(summary = "Execute SSH Client Multiple Commands")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Execute SSH Client Multiple Commands",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Execute SSH Commands",
					content = @Content)
	})
	@PostMapping("/commands/{command}")
	public ResponseEntity<ArrayList<CommandResults>> executeCommands(@PathVariable("command") String _command) {
		log.info("|"+name()+"|Request to execute command = "+_command);
		SSHClient cl = new SSHClient("demo", "password", "test.rebex.net", 22, 10);
		String[] commands = _command.split(",");
		for(String command : commands) {
			try {
				cl.clientStart();
				String result = cl.executeCommand(command);
				cl.clientStop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.ok(cl.getResultArray());
	}
	/**
	 * Cancel the Command
	 */
	@Operation(summary = "Cancel Command")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Command Cancelled",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Cancel the Command",
					content = @Content)
	})
	@DeleteMapping("/cancel/{referenceNo}")
	public ResponseEntity<HashMap<String,Object>> cancel(@PathVariable("referenceNo") String _referenceNo) {
		log.info("|"+name()+"|Request to Cancel the Command... ");
		HashMap<String,Object> status = new HashMap<String,Object>();
		status.put("Code", 404);
		status.put("Status", true);
		status.put("ReferenceNo", _referenceNo);
		status.put("Message","Unable to Cancel the Command!");
		return ResponseEntity.ok(status);
	}

	/**
	 * Update the Command
	 */
	@Operation(summary = "Update Command")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Update the Command",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Update the Command",
					content = @Content)
	})
	@PutMapping("/update/{referenceNo}")
	public ResponseEntity<HashMap<String,Object>> updatePayment(@PathVariable("referenceNo") String _referenceNo) {
		log.info("|"+name()+"|Request to Update Command... "+_referenceNo);
		HashMap<String,Object> status = new HashMap<String,Object>();
		status.put("Code", 404);
		status.put("Status", true);
		status.put("ReferenceNo", _referenceNo);
		status.put("Message","Unable to update Command!");
		return ResponseEntity.ok(status);
	}
 }