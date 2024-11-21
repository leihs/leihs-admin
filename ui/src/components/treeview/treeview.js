import React from 'react'
import { flushSync } from 'react-dom'
import { default as DefaultTreeView, flattenTree } from 'react-accessible-treeview'
import cx from 'classnames'
import s from './treeview.module.scss'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { data } from './sample'
import { faPlus, faMinus, faX, faInfo, faCircleInfo } from '@fortawesome/free-solid-svg-icons'
import NoImageSVG from './no-image.svg'
import { UncontrolledTooltip } from 'reactstrap'

// console.debug(data)
const folder = data

// const folder = {
//   name: '',
//   children: [
//     {
//       name: 'src',
//       children: [
//         {
//           name: 'index.js',
//
//           children: [{ name: 'index.js' }, { name: 'styles.css' }, { name: 'styles.css' }, { name: 'test.css' }]
//         },
//         { name: 'sample.js' }
//       ]
//     },
//     {
//       name: 'node_modules',
//       metadata: { isDirectory: true },
//       children: [
//         {
//           name: 'react-accessible-treeview',
//           children: [{ name: 'bundle.js' }]
//         },
//         { name: 'react', children: [{ name: 'bundle.js' }] }
//       ]
//     },
//     {
//       name: '.npmignore'
//     },
//     {
//       name: 'package.json'
//     },
//     {
//       name: 'webpack.config.js',
//       children: [
//         {
//           name: 'react-accessible-treeview',
//           children: [{ name: 'bundle.js' }]
//         },
//         { name: 'react', children: [{ name: 'bundle.js' }] }
//       ]
//     }
//   ]
// }

function TreeElement({ element, searchTerm, getNodeProps, handleSelect, level, isExpanded, isSelectable, onSelected }) {
  function handleClick(event) {
    handleSelect(event)
    onSelected(element)
    event.stopPropagation()
  }

  function highlightText(text, highlight) {
    if (!highlight) return text
    const regex = new RegExp(`(${highlight})`, 'gi')
    const parts = text.split(regex)
    return parts.map((part, index) =>
      part.toLowerCase() === highlight.toLowerCase() ? (
        <strong key={index}>
          <u>{part}</u>
        </strong>
      ) : (
        part
      )
    )
  }

  return (
    <div
      {...getNodeProps()}
      className={cx(isExpanded && s['expanded'], s['element'], element.children.length > 0 && s['pointer'])}
      style={{ paddingLeft: 20 * (level - 1) }}
    >
      <div className={cx(s['indent'], 'd-flex align-items-center')}>
        <div className={cx(s['controls'])} style={{ paddingLeft: 20 * (level - 1) }}>
          {element.children.length > 0 && (
            <>
              <FontAwesomeIcon className={s['icon']} icon={isExpanded ? faMinus : faPlus} />
              <span className="badge badge-secondary">{element.children.length}</span>
            </>
          )}
        </div>
        {element.metadata.thumbnail_url ? (
          <img src={element.metadata.thumbnail_url} width="32" height="32" />
        ) : (
          <NoImageSVG className={s['thumbnail']} />
        )}

        {onSelected ? (
          <span className="ml-3">{highlightText(element.name, searchTerm)} </span>
        ) : (
          <a className={cx('ml-3')} role="link" href={`/admin/categories/${element.metadata.id}`}>
            {highlightText(element.metadata.label || element.name, searchTerm)}

            {element.metadata.label && (
              <>
                <FontAwesomeIcon className="ml-2" icon={faCircleInfo} id={`id-${element.metadata.id}${element.id}`} />
                <UncontrolledTooltip placement="top" target={`id-${element.metadata.id}${element.id}`}>
                  This is the label for category: {element.name}
                </UncontrolledTooltip>
              </>
            )}
          </a>
        )}
        {onSelected ? (
          <button
            onClick={event => handleClick(event)}
            type="button"
            className={cx(s['visible-on-hover'], 'btn btn-primary ml-auto mr-4')}
          >
            Select
          </button>
        ) : (
          <span className={cx('ml-auto mr-4', element.metadata.models_count === 0 && 'text-danger')}>
            {element.metadata.models_count} Modelle
          </span>
        )}
      </div>
    </div>
  )
}

function TreeView({ data = folder, onSelected = null }) {
  const flattenedData = flattenTree(data)
  const ids = flattenedData.map(node => node.id)

  const treeRef = React.useRef(null)

  const [allIds, setAllIds] = React.useState(ids)
  const [searchTerm, setSearchTerm] = React.useState('')
  const [expandedIds, setExpandedIds] = React.useState([])
  const [filteredNodes, setFilteredNodes] = React.useState(flattenedData)
  const [noMatches, setNoMatches] = React.useState(false)
  const [idsMatch, setIdsMatch] = React.useState(true)

  function filterTree(node, searchTerm) {
    if (!node.children) {
      return node.name.toLowerCase().startsWith(searchTerm.toLowerCase()) ? node : null
    }

    const filteredChildren = node.children.map(child => filterTree(child, searchTerm)).filter(child => child !== null)

    if (filteredChildren.length > 0 || node.name.toLowerCase().startsWith(searchTerm.toLowerCase())) {
      return { ...node, children: filteredChildren }
    }

    return null
  }

  function reset() {
    setSearchTerm('')
    setFilteredNodes(flattenedData)
    setExpandedIds([])
    setAllIds(ids)
  }

  function filterNodes() {
    if (searchTerm === '') {
      reset()
      return
    }

    const tree = filterTree(data, searchTerm)
    if (tree === null) {
      setNoMatches(true)
      return
    } else {
      setNoMatches(false)
    }

    const flat = flattenTree(tree)
    const ids = flat.map(node => node.id)

    setFilteredNodes(flat)
    setAllIds(ids)
  }

  React.useEffect(() => {
    // setTimeout(() => {
    //   console.debug(treeRef.current.querySelectorAll('li'))
    // }, 1000)
  })

  React.useEffect(() => {
    // if (searchTerm !== '') setExpandedIds(allIds)
    // const allFilteredNodeIds = Array.from(new Set(filteredNodes.map(node => [node.id, ...node.children]).flat())).sort(
    //   (a, b) => a - b
    // )
    // const hasAllIds = allFilteredNodeIds.every((val, idx) => val === allIds[idx])
    // setTimeout(() => {
    // }, 200)
  }, [allIds, filteredNodes])

  return (
    <div className={cx(s['card-no-border'], 'card')}>
      <div className={cx(s['card-header-border'], 'card-header')}>
        <div className="btn-toolbar" role="toolbar" aria-label="Toolbar with button groups">
          <div className="input-group mr-4">
            <input
              type="text"
              value={searchTerm}
              onChange={event => setSearchTerm(event.target.value)}
              onKeyUp={_ => filterNodes()}
              autoComplete="off"
              className={cx('form-control', noMatches && 'text-danger')}
              placeholder="Search"
              aria-label="Category search field"
              onKeyDown={e => e.key === 'Enter' && setExpandedIds(allIds)}
            />
            <div className="input-group-append">
              <button className="btn btn-secondary" type="button" id="button-addon2" onClick={() => reset()}>
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
        ref={treeRef}
        data={filteredNodes}
        className={s['list']}
        expandedIds={expandedIds}
        onLoadData={_ => console.debug('data loaded')}
        nodeRenderer={({ element, getNodeProps, level, isExpanded, handleSelect }) => (
          <TreeElement
            searchTerm={searchTerm}
            element={element}
            getNodeProps={getNodeProps}
            level={level}
            isExpanded={isExpanded}
            handleSelect={handleSelect}
            onSelected={onSelected}
          />
        )}
      />
    </div>
  )
}

export default TreeView
