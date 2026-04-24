package org.shyu.marketserviceuser.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 更新收货地址请求
 */
@Data
public class UserAddressUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名长度不能超过50")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份长度不能超过50")
    private String province;

    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市长度不能超过50")
    private String city;

    @NotBlank(message = "区县不能为空")
    @Size(max = 50, message = "区县长度不能超过50")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址长度不能超过255")
    private String detailAddress;

    @Pattern(regexp = "^$|^\\d{6}$", message = "邮政编码格式不正确")
    private String postalCode;

    /**
     * 是否设为默认地址（可选；true时会切换默认）
     */
    private Boolean isDefault;
}
