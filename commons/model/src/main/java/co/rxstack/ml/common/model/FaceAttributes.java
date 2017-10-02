package co.rxstack.ml.common.model;

/**
 * @author mhachem on 9/27/2017.
 */
public class FaceAttributes {

	private String gender;
	private double age;

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public double getAge() {
		return age;
	}

	public void setAge(double age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "FaceAttributes{" + "gender='" + gender + '\'' + ", age=" + age + '}';
	}
}
