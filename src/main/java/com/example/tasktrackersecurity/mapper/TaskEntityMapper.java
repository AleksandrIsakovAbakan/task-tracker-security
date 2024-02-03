package com.example.tasktrackersecurity.mapper;

import com.example.tasktrackersecurity.api.v1.request.TaskRq;
import com.example.tasktrackersecurity.api.v1.response.TaskRs;
import com.example.tasktrackersecurity.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
@Mapper
public interface TaskEntityMapper {

    TaskEntityMapper INSTANCE = Mappers.getMapper(TaskEntityMapper.class);

    List<TaskRs> toDTO(List<TaskEntity> list);

    TaskRs toDTO(TaskEntity taskEntity);

    TaskEntity toModel(TaskRq taskRq);
}
