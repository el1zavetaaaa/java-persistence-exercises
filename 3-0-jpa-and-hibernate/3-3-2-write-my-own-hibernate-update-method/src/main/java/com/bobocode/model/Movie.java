package com.bobocode.model;

import com.bobocode.orm.annotations.Column;
import com.bobocode.orm.annotations.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "movies")
public class Movie {

    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "director")
    private String director;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
