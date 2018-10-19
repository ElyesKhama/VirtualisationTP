package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;

public class Controller {

	ProxmoxAPI api;

	public Controller(ProxmoxAPI api) {
		this.api = api;
	}

	// migrer un conteneur du serveur "srcServer" vers le serveur "dstServer"
	public void migrateFromTo(String srcServer, String dstServer, String ctID)
			throws LoginException, JSONException, IOException {
		System.out.println("Migration du CT " + ctID + " de " + srcServer + " vers " + dstServer);
		api.migrateCT(srcServer, ctID, dstServer);
	}

	// arr�ter le plus vieux conteneur sur le serveur "server"
	public void offLoad(String server) throws LoginException, JSONException, IOException, InterruptedException {
		List<LXC> cts = api.getCTs(server);
		// tri dans un ordre décroissant en fonction du uptime :
		Comparator<LXC> comparator = (x, y) -> (x.getUptime() < y.getUptime()) ? 1
				: ((x.getUptime() == y.getUptime()) ? 0 : -1);
		cts.sort(comparator);
		// celui avec le + grand uptime : le plus vieux : delete
		System.out.println("Arret du CT : " + cts.get(0).getVmid());
		api.stopCT(server, cts.get(0).getVmid());
		Thread.sleep(22000);
		System.out.println("Suppression du CT : " + cts.get(0).getVmid());
		api.deleteCT(server, cts.get(0).getVmid());
	}

}
