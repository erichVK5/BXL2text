public class FootprintElementArchetype
{

	long xOffsetNm = 0;
	long yOffsetNm = 0;

	public long Xposition()
	{
		return xOffsetNm;
	}

	public long Yposition()
	{
		return yOffsetNm;
	}

	public void FootprintElementArchetype()
	{
		xOffsetNm = 0;
		yOffsetNm = 0;
	}

        public void FootprintElementArchetype(long x, long y)
        {
                xOffsetNm = x;
                yOffsetNm = y;
        }

	public String toString()
	{
		return("x: " + xOffsetNm + ", y: " + yOffsetNm);
	}

	public String generateGEDAelement(long xOffset, long yOffset, float magnificationRatio)
	{
		return "";
	}

	public void populateElement(String moduleDefinition, boolean metric)
	{
		System.out.println("You're not supposed to see this.");		
	}

        public void populateElement(String moduleDefinition, boolean metric, long minimumViaDrillSize)
        {
                System.out.println("You're not supposed to see this.");
        }
	
}
