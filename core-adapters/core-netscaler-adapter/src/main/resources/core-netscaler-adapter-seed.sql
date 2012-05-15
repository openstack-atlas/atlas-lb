LOCK TABLES `adapter_cluster` WRITE;
INSERT INTO `adapter_cluster` (cluster_ipv6_cidr, description, name, password, username) VALUES ('fd24:f480:ce44:91bc::/64', 'User-facing web tier cluster','Helios-Cluster-1','626a47d059552219760a2d38a130cd80', 'user1');
UNLOCK TABLES;

LOCK TABLES `adapter_host` WRITE;
INSERT INTO `adapter_host` (endpoint, endpoint_active, host_status, name, password, username, cluster_id) VALUES ('http://10.70.177.9:4301/lbservice/v1',1,'ACTIVE_TARGET', 'Helios-1','626a47d059552219760a2d38a130cd80', 'user1', 1);
UNLOCK TABLES;

LOCK TABLES `adapter_virtual_ipv4` WRITE;
/*!40000 ALTER TABLE `adapter_virtual_ipv4` DISABLE KEYS */;
INSERT INTO `adapter_virtual_ipv4` (address, type, is_allocated, last_allocation, last_deallocation, cluster) 
                            VALUES ('10.70.176.100', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.101', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.102', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.103', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.104', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.105', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.106', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.107', 'PUBLIC', FALSE, NULL, NULL ,1)
                                  ,('10.70.176.108', 'PUBLIC', FALSE, NULL,NULL ,1)
                                  ,('10.70.176.109', 'PUBLIC', FALSE, NULL, NULL ,1) 
                                  ,('192.140.10.25', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.26', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.27', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.28', 'PRIVATE', FALSE, NULL,NULL ,1)
                                  ,('192.140.10.29', 'PRIVATE', FALSE, NULL, NULL ,1) 
                                  ,('192.140.10.30', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.31', 'PRIVATE', FALSE, NULL, NULL ,1)
                                  ,('192.140.10.32', 'PRIVATE', FALSE, NULL, NULL ,1);
/*!40000 ALTER TABLE `adapter_virtual_ipv4` ENABLE KEYS */;
UNLOCK TABLES;



