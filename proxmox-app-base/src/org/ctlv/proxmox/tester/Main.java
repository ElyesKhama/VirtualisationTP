package org.ctlv.proxmox.tester;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;
import org.ctlv.proxmox.api.Constants;

public class Main {

	public static void main(String[] args) throws LoginException, JSONException, IOException {

		ProxmoxAPI api = new ProxmoxAPI();
		String numCT = "1";
		String numCTID = "01";

		System.out.println("***** 1 Partie *******");
		// Listes les CTs par serveur
		
		  for (int i=1; i<=10; i++) { String srv ="srv-px"+i;
		  System.out.println("CTs sous "+srv); List<LXC> cts = api.getCTs(srv);
		  
		  for (LXC lxc : cts) { System.out.println("\t" + lxc.getName()); } }
		 

		// Cr�er un CT
		// api.createCT(Constants.SERVER1, Constants.CT_BASE_ID + numCTID, Constants.CT_BASE_NAME + numCT, 512);

		// Stop CT
		// api.stopCT(Constants.SERVER1, Constants.CT_BASE_ID + numCTID);
		// delete CT
		// api.deleteCT(Constants.SERVER1, Constants.CT_BASE_ID + numCTID);

		// get node informations
		System.out.println("***** Server Information :  *****");
		Node node = api.getNode(Constants.SERVER1);

		float cpu = node.getCpu() * 100;
		System.out.println("Cpu " + cpu + "%");
		float ram = (float) node.getMemory_used() / node.getMemory_total() * 100;
		System.out.println("Memory " + ram + "%");
		float disk = (float) node.getRootfs_used() / node.getRootfs_total() * 100;
		System.out.println("Disk " + disk + "%");

		// start the CT
		// api.startCT(Constants.SERVER1, Constants.CT_BASE_ID + numCTID);

		// Get Container information
	/*	System.out.println("***** Container Information :  *****");
		LXC lxc = api.getCT(Constants.SERVER1, Constants.CT_BASE_ID + numCTID);
		System.out.println("status :" + lxc.getStatus());
		System.out.println("Cpu :" + lxc.getCpu() + "%");
		System.out.println("Memory :" + (float) lxc.getMem() / lxc.getMaxmem() * 100 + "%");
		System.out.println("Disk :" + (float) lxc.getDisk() / lxc.getMaxdisk() * 100 + "%");*/


	}

}
