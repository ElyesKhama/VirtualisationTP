package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;

public class GeneratorMain {

	static Random rndTime = new Random(new Date().getTime());

	public static int getNextEventPeriodic(int period) {
		return period;
	}

	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}

	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (-Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int) next;
	}

	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {
		System.out.println("*** Démarrage du générateur ***");
		int lambda = 30;

		Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();

		ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		Random rndRAM = new Random(new Date().getTime());

		long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
		long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);

		int nbCT = 0;

		while (true) {
			System.out.println("** Génération n° "+ nbCT +" **");

			// 1. Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
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

			if (memOnServer1 < memAllowedOnServer1 && memOnServer2 < memAllowedOnServer2) {
				// choisir un serveur al�atoirement avec les ratios sp�cifi�s 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
					serverName = Constants.SERVER1;
				else
					serverName = Constants.SERVER2;
				
				// cr�er un contenaire sur ce serveur
				System.out.println("Création du CT numéro " + nbCT + " sur : " + serverName);
				api.createCT(serverName, Constants.CT_BASE_ID + nbCT, Constants.CT_BASE_NAME + nbCT, 512);
				System.out.println("wait ... ");
				Thread.sleep(22000);

				/*while (i < 1){
					LXC createdCT = api.getCT(serverName, Constants.CT_BASE_ID + nbCT);
					System.out.println("etat du CT : " + createdCT.getType());
				}*/
				System.out.println("starting the CT");
				api.startCT(serverName, Constants.CT_BASE_ID + nbCT);
				System.out.println("started ");

				nbCT++;
/*
				// planifier la prochaine création
				int timeToWait = getNextEventExponential(lambda); // par exemple une loi expo d'une moyenne de 30sec
				
				System.out.println("wait ... ");
				// attendre jusqu'au prochain �v�nement
				Thread.sleep(1000 * timeToWait);*/
				
			} else {
				System.out.println("Servers are loaded, waiting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME * 1000);
			}
		}

	}

}
