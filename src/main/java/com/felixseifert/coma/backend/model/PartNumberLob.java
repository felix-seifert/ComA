/*
 * Copyright 2019 Felix Seifert <mail@felix-seifert.com> (https://felix-seifert.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.felixseifert.coma.backend.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "part_number_lobs")
@Data
public class PartNumberLob implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(nullable = false, name = "id")
    private Integer id;

    @Lob
    private String comments;

    @OneToOne(mappedBy = "lobs", fetch = FetchType.LAZY)
    @JoinColumn(name = "part_number_object_id")
    private PartNumberObject partNumberObject;

    @Override
    public String toString() {
        return "PartNumberLob{" +
                "id=" + id +
                ", comments='" + comments + '\'' +
                ", partNumberObject=" + partNumberObject.getId() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartNumberLob)) return false;
        PartNumberLob that = (PartNumberLob) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 54;
    }
}
