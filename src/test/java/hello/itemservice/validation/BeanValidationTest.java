package hello.itemservice.validation;

import hello.itemservice.domain.item.Item;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class BeanValidationTest {

    @Test
    void beanValidation() {
        //  검증기생성
        //  다음코드와같이검증기를생성한다.
        //  이후스프링과통합하면우리가직접이런코드를작성하지는 않으므로, 이렇게사용하는구나정도만참고하자
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Item item = new Item();
        item.setItemName(" "); //공백
        item.setPrice(0);
        item.setQuantity(10000);


        //  검증실행
        //  검증대상( item)을직접검증기에넣고그결과를받는다.
        //  Set에는ConstraintViolation이라는검증 오류가담긴다.
        //  따라서결과가비어있으면검증오류가없는것이다.
        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        for (ConstraintViolation<Item> violation : violations) {
            System.out.println("violation = " + violation);
            System.out.println("violation = " + violation.getMessage());
        }

    }
}
