import React from 'react'
import { default as DefaultTreeView, flattenTree } from 'react-accessible-treeview'
import cx from 'classnames'
import s from './treeview.module.scss'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { data } from './sample'
import { faPlus, faMinus, faX } from '@fortawesome/free-solid-svg-icons'

console.debug(data)

const folder = {
  name: '',
  children: [
    {
      name: 'src',
      children: [{ name: 'index.js' }, { name: 'styles.css' }, { name: 'styles.css' }, { name: 'styles.css' }]
    },
    {
      name: 'node_modules',
      metadata: { isDirectory: true },
      children: [
        {
          name: 'react-accessible-treeview',
          children: [{ name: 'bundle.js' }]
        },
        { name: 'react', children: [{ name: 'bundle.js' }] }
      ]
    },
    {
      name: '.npmignore'
    },
    {
      name: 'package.json'
    },
    {
      name: 'webpack.config.js',
      children: [
        {
          name: 'react-accessible-treeview',
          children: [{ name: 'bundle.js' }]
        },
        { name: 'react', children: [{ name: 'bundle.js' }] }
      ]
    }
  ]
}

function TreeElement({ element, getNodeProps, level, handleSelect, isExpanded }) {
  return (
    <div
      {...getNodeProps()}
      className={cx(isExpanded && s['expanded'], s['element'])}
      style={{ paddingLeft: 20 * (level - 1) }}
    >
      <div className={cx(s['indent'], 'd-flex')}>
        <div className={cx(s['controls'])} style={{ paddingLeft: 20 * (level - 1) }}>
          {element.children.length > 0 && (
            <FontAwesomeIcon className={s['icon']} icon={isExpanded ? faMinus : faPlus} />
          )}
        </div>
        <span className="text-primary">{element.name}</span>
        <button type="button" className={cx(s['visible-on-hover'], 'btn btn-primary ml-auto mr-4')}>
          Select
        </button>
      </div>
    </div>
  )
}

function TreeView({ data = folder }) {
  const flatData = flattenTree(data)
  const allIds = flatData.map(el => el.id)

  const [expandedIds, setExpandedIds] = React.useState()

  return (
    <div className={cx(s['card-no-border'], 'card')}>
      <div className={cx(s['card-header-border'], 'card-header')}>
        <div class="btn-toolbar" role="toolbar" aria-label="Toolbar with button groups">
          <div class="input-group mr-4">
            <input
              type="text"
              autoComplete="off"
              class="form-control"
              placeholder="Recipient's username"
              aria-label="Recipient's username"
              aria-describedby="button-addon2"
            />
            <div class="input-group-append">
              <button class="btn btn-secondary" type="button" id="button-addon2">
                <FontAwesomeIcon className="mx-1" icon={faX} />
              </button>
            </div>
          </div>
          <button type="button" className="btn btn-outline-secondary" onClick={() => setExpandedIds(allIds)}>
            <FontAwesomeIcon className="mr-2" icon={faPlus} />
            open all
          </button>
          <button type="button" className="btn btn-outline-secondary ml-4" onClick={() => setExpandedIds([])}>
            <FontAwesomeIcon className="mr-2" icon={faMinus} />
            close all
          </button>
        </div>
      </div>

      <DefaultTreeView
        data={flatData}
        className={s['list']}
        aria-label="basic example tree"
        expandedIds={expandedIds}
        nodeRenderer={({ element, getNodeProps, level, isExpanded, handleSelect }) => (
          <TreeElement
            element={element}
            getNodeProps={getNodeProps}
            level={level}
            isExpanded={isExpanded}
            handleSelect={handleSelect}
          />
        )}
      />
    </div>
  )
}

export default TreeView
