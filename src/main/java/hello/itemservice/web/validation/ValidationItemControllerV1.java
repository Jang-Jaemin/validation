package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v1/items")
@RequiredArgsConstructor
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v1/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v1/addForm";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

        //검증 오류 결과를 보관
        Map<String, String> errors = new HashMap<>();

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            errors.put("itemName", "상품 이름은 필수입니다.");
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            errors.put("quantity", "수량은 최대 9,999 까지 허용합니다.");
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (!errors.isEmpty()) {
            log.info("errors = {} ", errors);
            model.addAttribute("errors", errors);
            return "validation/v1/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v1/items/{itemId}";
    }
}

//  MEMO
//  검증요구사항
//  상품 관리시스템에 새로운 요구사항이 추가 되었다.
//  요구사항: 검증로직추가

//  타입검증
//  가격, 수량에 문자가 들어가면 검증 오류 처리 필드검증
//  상품명: 필수, 공백X
//  가격: 1000원이상, 1백만원이하 수량: 최대 9999
//  특정 필드의 범위를 넘어서는 검증 가격 * 수량의 합은 10,000원 이상

//  지금까지 만든 웹 애플리케이션은 폼 입력시 숫자를 문자로 작성하거나 해서 검증 오류가 발생하면 오류 화면으로 바로 이동한다.
//  이렇게 되면 사용자는 처음부터 해당 폼으로 다시 이동해서 입력을 해야한다.
//  아마도 이런 서비스라면 사용자는 금방 떠나버릴것이다.
//  웹 서비스는 폼 입력시 오류가 발생하면, 고객이 입력한 데이터를 유지한 상태로 어떤 오류가 발생했는지 친절하게 알려주어야 한다.

//  컨트롤러의 중요한 역할중 하나는 HTTP 요청이 정상인지 검증하는것이다.
//  그리고 정상 로직보다 이런 검증 로직을 잘 개발하는것이 어쩌면 더 어려울수있다.
//  참고: 클라이언트검증, 서버검증
//  클라이언트 검증은 조작할 수 있으므로 보안에 취약하다.
//  서버만으로 검증하면, 즉각적인 고객 사용성이 부족해진다.
//  둘을 적절히 섞어서 사용하되, 최종적으로 서버 검증은 필수 API 방식을 사용하면 API 스펙을 잘 정의해서 검증오류를 API 응답결과에잘남겨주어야함
//  먼저검증을직접구현해보고, 뒤에서스프링과타임리프가제공하는검증기능을활용해보자.

//  상품 저장 성공
//  사용자가 상품 등록폼에서 정상 범위의 데이터를 입력하면, 서버에서는 검증 로직이 통과하고, 상품을 저장하고, 상품 상세 화면으로 redirect 한다.

//  상품 저장 검증 실패
//  고객이 상품 등록 폼에서 상품명을 입력하지 않거나, 가격, 수량등이 너무 작거나 커서 검증범위를 넘어서면, 서버 검증 로직이 실패해야한다. 
//  이렇게 검증에 실패한 경우 고객에게 다시 상품 등록폼을 보여주고, 어떤 값을 잘 못 입력했는지 친절하게 알려주어야 한다.
//  이제 요구 사항에 맞추어 검증 로직을 직접 개발해본다.

//