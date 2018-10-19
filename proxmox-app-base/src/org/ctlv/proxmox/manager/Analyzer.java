package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Analyzer {
	ProxmoxAPI api;
	Controller controller;

	public Analyzer(ProxmoxAPI api, Controller controller) {
		this.api = api;
		this.controller = controller;
	}

	public void analyze(Map<String, List<LXC>> myCTsPerServer) throws LoginException, JSONException, IOException, InterruptedException {

		ProxmoxAPI api = new ProxmoxAPI();

		// Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
		long memOnServer1 = 0;
		List<LXC> cts1 = api.getCTs(Constants.SERVER1);
		for (LXC lxc : cts1) {
			if (lxc.getName().contains("B1")) {
				System.out.println(lxc.getName());
				memOnServer1 += lxc.getMem();
			}
		}
		float memPercent1 = (float) memOnServer1 / api.getNode(Constants.SERVER1).getMemory_total() * 100;
		System.out.println("mémoire occuppée par mes CT sur le serveur 1 (srv-px5) : " + memPercent1 + "%");

		long memOnServer2 = 0;
		List<LXC> cts2 = api.getCTs(Constants.SERVER2);
		for (LXC lxc : cts2) {
			if (lxc.getName().contains("B1")) {
				memOnServer2 += lxc.getMem();
			}
		}
		float memPercent2 = (float) memOnServer2 / api.getNode(Constants.SERVER1).getMemory_total() * 100;
		System.out.println("mémoire occuppée par mes CT sur le serveur 2 (srv-px6) : " + memPercent2 + "%");

		// M�moire autoris�e sur chaque serveur
		long memAllowedOnServer1Migration = (long) (api.getNode(Constants.SERVER1).getMemory_total()
				* Constants.MIGRATION_THRESHOLD);
		long memAllowedOnServer2Migration = (long) (api.getNode(Constants.SERVER2).getMemory_total()
				* Constants.MIGRATION_THRESHOLD);
		
		long memAllowedOnServer1OffLoad = (long) (api.getNode(Constants.SERVER1).getMemory_total()
				* Constants.DROPPING_THRESHOLD);
		long memAllowedOnServer2OffLoad = (long) (api.getNode(Constants.SERVER2).getMemory_total()
				* Constants.DROPPING_THRESHOLD);

		// Analyse et Actions
		if(memOnServer1 > memAllowedOnServer1OffLoad && memOnServer2 > memAllowedOnServer2OffLoad) {
			System.out.println("La mémoire des 2 serveurs dépasse 12% : dropping ..." );
			controller.offLoad(Constants.SERVER1);
			controller.offLoad(Constants.SERVER2);
		}
		
		if (memOnServer1 > memAllowedOnServer1Migration) {
			controller.migrateFromTo(Constants.SERVER1, Constants.SERVER2, Constants.CT_BASE_ID + "0");
		}

		if (memOnServer1 > memAllowedOnServer2Migration) {
			controller.migrateFromTo(Constants.SERVER2, Constants.SERVER1, Constants.CT_BASE_ID + "0");
		}

	}

}
