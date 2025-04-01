package org.matsim.pt2matsim.test;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;

public class CreateGridNetwork {

    /**
     * create a grid network with 10x10 grid
     * each link length is 100 m
     */

    public static void main(String[] args) {

        Config config = ConfigUtils.createConfig();
        Scenario sc = ScenarioUtils.createScenario(config);
        Network network = sc.getNetwork();

//		// create node
//		NodeImpl node[][] =  new NodeImpl[10][10];
//		for (int i=0;i<10;i++){
//			for (int j=0;j<10;j++){
//				IdImpl id = new IdImpl(Integer.toString(i)+Integer.toString(j));
//				System.out.println(id);
//				CoordImpl coord = new CoordImpl(i*100, j*100);
//				node[i][j] = new NodeImpl(id);
//				node[i][j].setCoord(coord);
//				network.addNode(node[i][j]);
//			}
//		}
//		// create links
//		for (int i=0;i<10;i++){
//			for (int j=0;j<10;j++){
//				if (j != 9){
//					IdImpl id_h1 = new IdImpl(Integer.toString(i)+Integer.toString(j)+"h1");
//					network.createAndAddLink(id_h1, (Node)node[i][j], (Node)node[i][j+1], 100.0, 27.8, 4000, 2);
//					IdImpl id_h2 = new IdImpl(Integer.toString(j)+Integer.toString(i)+"h2");
//					network.createAndAddLink(id_h2, (Node)node[i][j+1], (Node)node[i][j], 100.0, 27.8, 4000, 2);
//				}
//				if (i !=9){
//					IdImpl id_v1 = new IdImpl(Integer.toString(i)+Integer.toString(j)+"v1");
//					network.createAndAddLink(id_v1, (Node)node[i][j], (Node)node[i+1][j], 100.0, 27.8, 4000, 2);
//					IdImpl id_v2 = new IdImpl(Integer.toString(j)+Integer.toString(i)+"v2");
//					network.createAndAddLink(id_v2, (Node)node[i+1][j], (Node)node[i][j], 100.0, 27.8, 4000, 2);
//				}
//			}
//		}
        // create node
        int iCol = 5;
        int iRow = 5;
        double baseX = 12640311;double baseY = 2598999.0;
        Node node[][] =  new Node[iRow][iCol];
        for (int i=0;i<iRow;i++){
            for (int j=0;j<iCol;j++){
                node[i][j] = NetworkUtils.createNode(Id.createNodeId(Integer.toString(i)+"_"+Integer.toString(j)),
                        new Coord(baseX-2000+i*1000., baseY-2000+j*1000.));
                network.addNode(node[i][j]);
            }
        }

        // create links
        for (int i=0;i<iRow;i++){
            for (int j=0;j<iCol;j++){

                if (i <iRow-1){
                    NetworkUtils.createAndAddLink(network,Id.createLinkId(node[i][j].getId().toString()+">"+node[i+1][j].getId().toString()),
                                    node[i][j],node[i+1][j],1000.0,27.8,1000,2);
                    NetworkUtils.createAndAddLink(network,Id.createLinkId(node[i+1][j].getId().toString()+">"+node[i][j].getId().toString()),
                            node[i+1][j],node[i][j],1000.0,27.8,1000,2);
                }
                if (j < iCol-1){

                    NetworkUtils.createAndAddLink(network,Id.createLinkId(node[i][j].getId().toString()+">"+node[i][j+1].getId().toString()),
                            node[i][j],node[i][j+1],1000.0,27.8,1000,2);
                    NetworkUtils.createAndAddLink(network,Id.createLinkId(node[i][j+1].getId().toString()+">"+node[i][j].getId().toString()),
                            node[i][j+1],node[i][j],1000.0,27.8,1000,2);

                }
            }
        }
        new NetworkCleaner().run(network);
        new NetworkWriter(network).write("testInput2/gridNetwork.xml");
//        Links2ESRIShape links2ESRIShape = new Links2ESRIShape(network,"/Users/convel/Desktop/gridNetwork.shp","EPSG:4326");
//        links2ESRIShape.write();
    }

}