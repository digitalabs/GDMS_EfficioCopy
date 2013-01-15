package org.icrisat.gdms.upload;

public class MappingPopulationBean {
	
	private int dataset_id;
	//private long mp_id;
	private String mapping_type;
	private int parent_a_gid;
	private int parent_b_gid;
	private int population_size;
	private String population_type;
	private String mapdata_desc;
	private String scoring_scheme;
	private int map_id;
	
	public int getDataset_id() {
		return dataset_id;
	}
	public void setDataset_id(int dataset_id) {
		this.dataset_id = dataset_id;
	}
	/*public long getMcid() {
		return mcid;
	}
	public void setMcid(long mcid) {
		this.mcid = mcid;
	}
	public String getParent_a() {
		return parent_a;
	}
	public void setParent_a(String parent_a) {
		this.parent_a = parent_a;
	}
	public String getParent_b() {
		return parent_b;
	}
	public void setParent_b(String parent_b) {
		this.parent_b = parent_b;
	}*/
	public int getPopulation_size() {
		return population_size;
	}
	public void setPopulation_size(int population_size) {
		this.population_size = population_size;
	}
	public String getPopulation_type() {
		return population_type;
	}
	public void setPopulation_type(String population_type) {
		this.population_type = population_type;
	}
	public String getMapdata_desc() {
		return mapdata_desc;
	}
	public void setMapdata_desc(String mapdata_desc) {
		this.mapdata_desc = mapdata_desc;
	}
	public String getScoring_scheme() {
		return scoring_scheme;
	}
	public void setScoring_scheme(String scoring_scheme) {
		this.scoring_scheme = scoring_scheme;
	}
	
	/*public long getMp_id() {
		return mp_id;
	}
	public void setMp_id(long mp_id) {
		this.mp_id = mp_id;
	}*/
	public int getParent_a_gid() {
		return parent_a_gid;
	}
	public void setParent_a_gid(int parent_a_gid) {
		this.parent_a_gid = parent_a_gid;
	}
	public int getParent_b_gid() {
		return parent_b_gid;
	}
	public void setParent_b_gid(int parent_b_gid) {
		this.parent_b_gid = parent_b_gid;
	}
	public int getMap_id() {
		return map_id;
	}
	public void setMap_id(int map_id) {
		this.map_id = map_id;
	}
	public String getMapping_type() {
		return mapping_type;
	}
	public void setMapping_type(String mapping_type) {
		this.mapping_type = mapping_type;
	}
	
	
	
	
	
}