package com.seckill.admin.service;

import com.seckill.admin.dto.req.ActivityCreateReqDTO;
import com.seckill.admin.dto.req.ActivityUpdateReqDTO;
import com.seckill.admin.dto.resp.ActivityRespDTO;
import java.util.List;

public interface ActivityAdminService {

  ActivityRespDTO create(ActivityCreateReqDTO req);

  ActivityRespDTO update(Long id, ActivityUpdateReqDTO req);

  ActivityRespDTO getById(Long id);

  List<ActivityRespDTO> list();

  void initCache(Long id);
}
