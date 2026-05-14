package com.seckill.common.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
@Schema(description = "用户")
public class UserDO {
  @TableId(type = IdType.AUTO)
  @Schema(description = "用户ID")
  private Long id;

  @Schema(description = "用户名")
  private String username;

  @Schema(description = "密码")
  private String password;

  @Schema(description = "昵称")
  private String nickname;

  @TableField(fill = FieldFill.INSERT)
  @Schema(description = "创建时间")
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  @Schema(description = "更新时间")
  private LocalDateTime updateTime;

  @TableLogic
  @Schema(description = "逻辑删除: 0-未删除 1-已删除")
  private Integer isDeleted;
}
