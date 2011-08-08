package me.simplex.garden;

import java.util.List;
import java.util.Random;

import me.simplex.garden.Voronoi.DistanceMetric;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class Generator extends ChunkGenerator {
	
	private int CoordinatesToByte(int x, int y, int z) {
		return (x * 16 + z) * 128 + y;
	}

	@Override
	public byte[] generate(World world, Random random, int x_chunk, int z_chunk) {
		SimplexOctaveGenerator gen_hills					=  new SimplexOctaveGenerator(new Random(world.getSeed()), 4);
		SimplexOctaveGenerator gen_ground  					=  new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
		SimplexOctaveGenerator gen_highland					=  new SimplexOctaveGenerator(new Random(world.getSeed()), 16);
		SimplexOctaveGenerator gen_spikes					=  new SimplexOctaveGenerator(new Random(world.getSeed()), 32);
		SimplexOctaveGenerator gen_spikes_height			=  new SimplexOctaveGenerator(new Random(world.getSeed()), 32);
		
		Voronoi voronoi_gen_high = new Voronoi(128, true, world.getSeed(), 2, DistanceMetric.Quadratic, 1);
		Voronoi voronoi_gen_low = new Voronoi(64, true, world.getSeed(), 4, DistanceMetric.Squared, 1);

		gen_spikes.setScale(1/32.0);
		gen_spikes_height.setScale(1/64.0);
		gen_hills.setScale(1/64.0);
		gen_ground.setScale(1/256.0);
		gen_highland.setScale(1/1024.0);
				
		byte[] chunk_data = new byte[32768];
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
			
				genUnderground(x, z, chunk_data);
//
//				genSurface_Highlands(x, z, chunk_data, x_chunk, z_chunk, gen_highland);
//				
//				genSurface_Hills(x, z, chunk_data, x_chunk, z_chunk, gen_hills, 16);
//				
				genSurface_Ground(x, z, chunk_data, x_chunk, z_chunk, gen_ground);
				
//				genSurface_Spikes(x, z, chunk_data, x_chunk, z_chunk, gen_spikes, gen_spikes_height);
				
				genSurface_Noise_High(x, z, x_chunk,z_chunk, chunk_data, voronoi_gen_high);
				
				genSurface_Noise_Low(x, z, x_chunk,z_chunk, chunk_data, voronoi_gen_low);	
				
	//			genWater(x, z, chunk_data);
//				
				genSurface_TopLayer(x,z, chunk_data);
				
				genFloor(x, z, chunk_data);
			}
		}
		return chunk_data;
	}
	
	private void genFloor(int x, int z, byte[] chunk_data){
		for (int y = 0; y < 5; y++) {			
			if (y<3) { //build solid bedrock floor
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.BEDROCK.getId();
			}
			else { // build 2 block height mix floor
				int rnd = new Random().nextInt(100);
				if (rnd < 40) {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.BEDROCK.getId();
				}
				else {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
				}
			}
		}
	}
	
	private void genUnderground(int x, int z, byte[] chunk_data) {
		for (int y = 5; y < 30 ; y++) {
			chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
		}
	}
	
	private void genSurface_Highlands(int x, int z, byte[] chunk_data, int xChunk, int zChunk, SimplexOctaveGenerator gen) {
		double noise = gen.noise(x+xChunk*16, z+zChunk*16, 0.1, 0.1)*25;
		int limit = (int) (30+noise);
		for (int y = 30; y < limit; y++) {
			if (y < 90 && y >= 0) {
				if (y+5 >= limit) {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.DIRT.getId();
				}
				else {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
				}
			}
		}
	}
	
	private void genSurface_Hills(int x, int z, byte[] chunk_data, int xChunk, int zChunk, SimplexOctaveGenerator gen, int multiply) {
		double noise = gen.noise(x+xChunk*16, z+zChunk*16, 0.6, 0.6)*multiply;		
		int limit = (int) (35+noise);
		for (int y = 30; y < limit; y++) {
			if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
				if (y+5 >= limit) {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.DIRT.getId();
				}
				else {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
				}
				
			}
		}
	}
	
	private void genSurface_Ground(int x, int z, byte[] chunk_data, int xChunk, int zChunk, SimplexOctaveGenerator gen) {
		double noise = gen.noise(x+xChunk*16, z+zChunk*16, 0.01, 0.5)*15;
		int limit = (int) (35+noise);
		for (int y = 30; y < limit; y++) {
			if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
				if (y+5 >= limit) {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.DIRT.getId();
				}
				else {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
				}
			}
		}
	}
	
	private void genSurface_Spikes(int x, int z, byte[] chunk_data, int xChunk, int zChunk, SimplexOctaveGenerator gen, SimplexOctaveGenerator gen_height) {
		double noise = gen.noise(x+xChunk*16, z+zChunk*16, 1,1)*100;
		double maxheight = 30+gen.noise(x+xChunk*16, z+zChunk*16, 1,1)*5;
		int limit = (int) (25+noise);
		for (int y = 25; y < limit; y++) {
			if (y <= maxheight && y <= 126) {
				if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
					if (y+2 >= limit) {
						chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.DIRT.getId();
					}
					else {
						chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
					}
				}
			}
		}
	}
	
	private void genSurface_Noise_High(int x, int z, int xChunk, int zChunk, byte[] chunk_data, Voronoi noisegen) {
		double noise = noisegen.get((x+xChunk*16)/128.0f, (z+zChunk*16)/128.0f)*80;
		int limit = (int) (35+noise);
			for (int y = 10; y < limit; y++) {
				if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
					if (y+new Random().nextInt(3) >= limit) {
						chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.DIRT.getId();
					}
					else {
						chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
					}
				}
			}
	}
	
	private void genSurface_Noise_Low(int x, int z, int xChunk, int zChunk, byte[] chunk_data, Voronoi noisegen) {
		double noise = 20+noisegen.get((x+xChunk*16)/32.0f, (z+zChunk*16)/32.0f)*150;
		int y_start = 0;
		for (int y = 127; y >= 0; y--) {
			if (chunk_data[CoordinatesToByte(x, y, z)] != (byte) Material.AIR.getId()) {
				y_start=y;
				break;
			}
		}
		for (int y = y_start; y >= y_start-noise; y--) {
			if (y >=0) {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.WOOD.getId();
			}
		}	
	}
	
	private void genSurface_TopLayer(int x, int z, byte[] chunk_data) {
		int y = 127;
		while (true) {
			if (chunk_data[CoordinatesToByte(x, y, z)] != 0) {
				if (chunk_data[CoordinatesToByte(x, y, z)] == Material.STATIONARY_WATER.getId()) {
					return;
				}
				
				if (y==36 || y==35) {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.SAND.getId();
					return;
				}
				
				if (y >= 70+new Random().nextInt(10) ) {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
					return;
				}
				
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.GRASS.getId();
				return;
			}
			y--;
			if (y <= 0) {
				return;
			}
		}
	}
	
	private void genWater(int x, int z, byte[] chunk_data) {
		int y = 35;
		while (true) {
			if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STATIONARY_WATER.getId();
			}
			y--;
			if (y <= 20) {
				return;
			}
		}
	}
	
	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return super.getDefaultPopulators(world);
	}
	
	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}

}
