package validation;

public abstract class FormValidator<B extends FormBean> {
	
	public abstract ValidationResult validateFormBean(B form);
	
}
